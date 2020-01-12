(ns hackerpaper.hiccup-tools)

(defn tag
  "Returns tag of hiccup-style node."
  [node]
  (first node))

(defn attrs
  "Returns attributes of hiccup-style node."
  [node]
  (second node))

(defn children
  "Returns seq of children from hiccup-style node."
  [node]
  (drop 2 node))

(defn walk
  "Walk a hiccup structure"
  [node]
  (cons node
        (mapcat identity
                (for [child (children node)
                      :when (not (string? child))]
                  (walk child)))))

(defn- selector-spec->pred
  "Maps the given selector spec to a node predicate."
  [spec]
  (cond
    (keyword? spec)
    (fn [node] (= (tag node) spec))

    (map? spec)
    (fn [node]
      (let [node-attrs (attrs node)]
        (every? (fn [[k v]]
                  (and (contains? node-attrs k)
                       (= v (node-attrs k))))
                spec)))

    (fn? spec)
    spec))

(defn selector
  "Returns a predicate that returns true for hiccup nodes matching the given
  spec.

  Spec elememnts can be keywords (matched to tag name), maps (matched to
  attributes) or node predicates.

  Individual spec predicates are joined with `and`."
  [[& specs]]
  (let [preds (mapv selector-spec->pred specs)]
    (fn [node]
      (every? #(% node) preds))))

(defn select
  "Convenience function for (-> (hwalk node) (as-> xs (filter (selector selector-specs) xs)) first)."
  [node selector-specs]
  (-> (walk node)
      (as-> xs (filter (selector selector-specs) xs))
      first))
