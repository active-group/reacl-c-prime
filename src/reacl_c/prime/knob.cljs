(ns reacl-c.prime.knob
  (:require
   [reacl-c.prime.internal.lift :as lift :include-macros true]
   [reacl-c.core :as c :include-macros true]
   [schema.core :as s]
   ["primereact/knob" :as knob]))

(lift/def-react raw knob/Knob)

(defn- return-value [_value ev]
  (c/return :state (.-value ev)))

(c/defn-item input :- s/Int [& [attrs]]
  (c/with-state-as v
    (raw (assoc attrs
                :value v
                :onChange return-value))))
