/*
 * Copyright 2013 Twitter Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain
 * a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package simplified.storehaus.cache

/**
  * A Mutable cache generated by wrapping an immutable cache.
  */

class MutableFromImmutableCache[K, V](cache: Cache[K, V])(exhaustFn: Option[K] => Unit)
    extends MutableCache[K, V] {
  protected val cacheRef = Atomic[Cache[K, V]](cache)

  override def get(k: K): Option[V] = cacheRef.get().get(k)
  override def +=(kv: (K, V)): this.type = {
    exhaustFn(cacheRef.effect { _.put(kv) }._1)
    this
  }
  override def hit(k: K): Option[V] = cacheRef.update { _.hit(k) }.get(k)
  override def evict(k: K): Option[V] = cacheRef.effect { _.evict(k) }._1
  override def empty: MutableFromImmutableCache[K, V] =
    new MutableFromImmutableCache(cache.empty)(exhaustFn)
  override def clear: this.type = { cacheRef.update { _.empty }; this }
  override def contains(k: K): Boolean = cacheRef.get().contains(k)
  override def -=(k: K): this.type = { cacheRef.update { _ - k }; this }
  override def getOrElseUpdate(k: K, v: => V): V = {
    lazy val cachedV = v
    cacheRef.update { _.touch(k, cachedV) }.get(k).getOrElse(cachedV)
  }
  override def iterator: Iterator[(K, V)] = cacheRef.get().iterator
}
