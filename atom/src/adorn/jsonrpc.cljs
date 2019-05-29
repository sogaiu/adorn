(ns adorn.jsonrpc)

(defn to-str
  ([meth params]
   (to-str meth params 1))
  ([meth params id]
   (.stringify js/JSON
               (clj->js {"jsonrpc" "2.0"
                         "method" meth
                         "params" params
                         "id" id}))))

(defn from-str
  [jr-str]
  (js->clj (.parse js/JSON jr-str)))
