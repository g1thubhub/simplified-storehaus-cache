package simplified.storehaus.cache

import scala.collection.mutable.{ Map => MutableMap }

object MutableMapCache {
  def apply[K, V](m: MutableMap[K, V]): MutableMapCache[K, V] = new MutableMapCache(m)
}
/**
  * MutableCache backed by a scala mutable Map.
  */

class MutableMapCache[K, V](m: MutableMap[K, V]) extends MutableCache[K, V] {
  override def get(k: K): Option[V] = m.get(k)
  override def +=(kv: (K, V)): this.type = { m += kv; this }
  override def hit(k: K): Option[V] = m.get(k)
  override def evict(k: K): Option[V] = {
    val ret = m.get(k)
    m -= k
    ret
  }
  override def empty: MutableMapCache[K, V] = new MutableMapCache(m.empty)
  override def clear: this.type = { m.clear; this }
  override def iterator: Iterator[(K, V)] = m.iterator
}
