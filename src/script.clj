(ns script
  (:require
    [augistints.core :as ac]
    [clojure.data.json :as cdj]
    [rewrite-clj.zip :as rz])
  (:gen-class))

(set! *warn-on-reflection* true)

(defn find-defn-zloc-at
  [forms-str row col]
  (let [curr-zloc (-> (rz/of-string forms-str
                                    {:track-position? true})
                      (rz/find-last-by-pos [row col]))
        initial-char (first (rz/string curr-zloc))
        curr-zloc (if (= initial-char \()
                    (rz/down curr-zloc)
                    curr-zloc)]
    (loop [curr-zloc curr-zloc]
      (when curr-zloc
        (let [lm-zloc (rz/leftmost curr-zloc)
              up-zloc (rz/up lm-zloc)]
          (if (#{'defn 'defn-} (rz/sexpr lm-zloc))
            up-zloc
            (recur up-zloc)))))))

(defn -main [& args]
  ;; XXX: expects json rpc string on stdin, e.g.
  ;;        {"jsonrpc": "2.0",
  ;;         "method": "inlinedef",
  ;;         "params": ["(defn my-fn [a] (+ a 1))", 3, 4],
  ;;         "id": 1}
  (let [slurped (slurp *in*)
        {:keys [method params id] :as input}
        (cdj/read-str slurped :key-fn keyword)
        [text row col] params
        defn-zloc (find-defn-zloc-at text row col)
        [new-text new-range]
        (if defn-zloc
           [(-> (rz/string defn-zloc)
                (ac/prepend-to-defn-body (ac/make-inline-def-with-meta-gen
                                          {:adorn/inlinedef true}))
                ac/cljfmt)
            (rz/position-span defn-zloc)]
           [nil nil])]
    ;; XXX: check for errors?
    (-> (assoc {"jsonrpc" "2.0", "id" id}
                "result" [new-text new-range])
        cdj/write-str
        println)
    (flush)
    (System/exit 0)))

(comment

  ;;{"jsonrpc": "2.0", "method": "inlinedef", "params": ["(defn my-fn [a] (+ a 1))" 3 4], "id": 1}
  )
