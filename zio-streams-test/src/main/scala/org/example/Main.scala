package org.example

import zio.ZIOAppDefault
import zio.stream.ZStream
import zio.ZIO
import java.time.Instant
import zio.durationInt

object Main extends ZIOAppDefault {
  override def run =
    ZStream
      .fromIterable('a' to 'z')
      .mapZIOPar(5)(c => process("stage 1", c))
      .throttleShape(1, 1.second)(_ => 1)
      .mapZIO(c => process("stage 2", c))
      .runDrain

  def process(stage: String, c: Char): ZIO[Any, Nothing, Char] =
    ZIO.succeed(println(s"${Instant.now()} $stage: $c")) *>
      ZIO.succeed(c)
}
