package org.example

import zio.kafka.consumer.Consumer
import zio.kafka.consumer.CommittableRecord
import zio.stream.ZStream
import zio.kafka.consumer.Subscription
import zio.kafka.serde.Deserializer
import Pipeline._
import zio.ZIO
import zio.IO
import zio.kafka.producer.Producer
import zio.kafka.serde.Serializer

/*
Usage:
  V1
  Pipeline
    .create(consumer, topic)
    .mapPar(5)(parseOrder)
    .filter(positiveAmount)
    .map(persistOrder)
    .commit
    .run

  V2
  val parseOrder = Stage(
    "parseOrder",
    parseOrder,
    parallelism = Some(8),
    onError = OnError.MoveToDLQ("dlq-parse-order"),
    logBefore = true,
    logAfter = true
  )
  ...

  Pipeline
    .create(consumer, topic)
    .stage(parseOrder)
    .stage(filterPositiveAmount)
    .stage(persistOrder)
    .commit
    .run
 */
class Pipeline[A](stream: Stream[A], producer: Producer):

  def stage[B](stage: Stage[A, B]): Pipeline[B] =
    map(stream =>
      stage.parallelism match
        case Some(parallelism) =>
          stream.mapZIOPar(parallelism)(applyFunction(stage))
        case None              =>
          stream.mapZIO(applyFunction(stage))
    )

  def commit: Pipeline[A] =
    map(_.tap(ctx => ctx.record.offset.commit))

  def map[B](f: Stream[A] => Stream[B]): Pipeline[B] =
    new Pipeline[B](f(stream), producer)

  def run: ZIO[Any, Throwable, Unit] =
    stream.runDrain

  private def mapPayload[B](
    record: CommittableRecord[String, String],
    value: A,
    stage: Stage[A, B]
  ): ZIO[Any, Throwable, Context[B]] =
    (for {
      _      <- ZIO.when(stage.logBefore)(
                  ZIO.logInfo(
                    s"${recordToString(record)} before '${stage.name}' '${value}'"
                  )
                )
      result <- stage.run(value)
      _      <- ZIO.when(stage.logAfter)(
                  ZIO.logInfo(
                    s"${recordToString(record)} after '${stage.name}' '${result}'"
                  )
                )
    } yield Context(record, result))
      .catchAll(error =>
        for {
          _ <-
            ZIO.logError(
              s"${recordToString(record)} failed at '${stage.name}' with '${error.message}'"
            )
          _ <- stage.onError match
                 case OnError.MoveToDLQ(topicDlq) =>
                   for {
                     _ <- producer.produce(
                            topicDlq,
                            record.key,
                            record.value,
                            Serializer.string,
                            Serializer.string
                          )
                     _ <- ZIO.logInfo(
                            s"${recordToString(record)} moved to '${topicDlq}'"
                          )
                   } yield ()
                 case OnError.Ignore              =>
                   ZIO.succeed(())
                 case OnError.Stop                =>
                   ZIO.fail(new RuntimeException("Stop"))
        } yield Context(record, None)
      )

  private def applyFunction[B](stage: Stage[A, B])(ctx: Context[A]): ZIO[Any, Throwable, Context[B]] =
    ctx.payload match
      case None        => ZIO.succeed(Context(ctx.record, None))
      case Some(value) => mapPayload(ctx.record, value, stage)

  private def recordToString(record: CommittableRecord[String, String]): String =
    s"[${Option(record.key).getOrElse("null")} ${record.partition}:${record.offset.offset}]"

end Pipeline

object Pipeline:
  enum OnError:
    case Ignore
    case Stop
    case MoveToDLQ(topic: String)

  case class Stage[A, B](
    name: String,
    run: A => IO[Error, Option[B]],
    parallelism: Option[Int] = None,
    onError: OnError = OnError.Ignore,
    logBefore: Boolean = false,
    logAfter: Boolean = false
  )

  case class Error(message: String)

  type Stream[A] = ZStream[Any, Throwable, Context[A]]

  case class Context[A](record: CommittableRecord[String, String], payload: Option[A]) {
    def map[B](f: A => B): Context[B] =
      Context(record, payload.map(f))
  }

  def create(consumer: Consumer, producer: Producer, topic: String): Pipeline[String] =
    new Pipeline[String](
      consumer
        .plainStream(
          Subscription.topics(topic),
          Deserializer.string,
          Deserializer.string
        )
        .map(record => Context[String](record, Some(record.value))),
      producer
    )

end Pipeline
