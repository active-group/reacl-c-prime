(ns reacl-c.prime.internal.util
  (:require
   [reacl-c.core :as c :include-macros true]))

(defn opt-update [m k f & args]
  (if (contains? m k)
    (apply update m k f args)
    m))

(defn item-or-fn [i-f embed]
  (when i-f
    (if (c/item? i-f)
      (embed i-f)
      (comp embed i-f))))

(defn js-update [obj key f]
  (js/Object.assign #js {}
                    obj
                    (js-obj key (f (aget obj key)))))
