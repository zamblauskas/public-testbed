package zamblauskas

import zio.schema.optics.ZioOpticsBuilder
import zio.schema.Schema.CaseClass2

object DerivationApp extends App:
  given ordering: Ordering[Order] = orderSchema.ordering

  println(orders.sorted)

  // type is
  // orderSchema.Accessors[[F, S, A] =>> zio.optics.Optic[S, S, A, zio.optics.OpticFailure, zio.optics.OpticFailure, A, S], [F, S, A] =>> zio.optics.ZPrism[S, S, A, A], [S, A] =>> zio.optics.ZTraversal[S, S, A, A]]
  val optics = orderSchema.makeAccessors(ZioOpticsBuilder)

  // it looks like for optics, we need to cast it manually to a more specific type
  // e.g. CaseClass2[String, List[OrderLine], Order]
  // which defeats the purpose IMO
