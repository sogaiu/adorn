(ns adorn.core
  (:require [adorn.atom :as aa]
            [adorn.dispose :as ad]
            [adorn.jsonrpc :as aj]))

(def cp
  (js/require "child_process"))

(def path
  (js/require "path"))

(def adorn-path
  (let [pkg-dir-path (->> (aa/path-for-pkg "adorn")
                          (.dirname path)
                          (.dirname path))]
    (str pkg-dir-path "/bin/adorn")))

(defn inline-def
  []
  (aa/info-message "invoking helper...")
  (let [p (.spawn cp adorn-path #js [] #js {})
        stdout (.-stdout p)
        stdin (.-stdin p)]
    ;; receiving response from helper
    (.on stdout "data"
      (fn [data]
        ;; XXX: handle error case
        (let [[text text-range]
              (get (aj/from-str data)
                   "result")]
          ;; XXX: when no text returned, tell user no defn/defn- detected?
          (when text
            ;; row, col values need to be one less for atom api
            (let [[[start-row start-col] [end-row end-col]] text-range]
              (.setTextInBufferRange (aa/current-editor)
                #js [#js [(dec start-row) (dec start-col)]
                     #js [(dec end-row) (dec end-col)]]
                text))))))
    (.on stdout "end"
      (fn []
        (println "finished!")))
    (.on p "close"
      (fn [code]
        (println (str "exit code: " code))))
    (.on p "error"
      (fn [err]
        (println (str "err: " err))))
    ;; sending to helper
    (.end stdin
      (aj/to-str "inlinedef"
        ;; buffer text, helper counts from 1 for both row and column values
        [(aa/current-buffer) (inc (aa/current-row)) (inc (aa/current-col))]))))

(defn activate
  [s]
  (ad/reset-disposables!)
  ;;
  (ad/command-for "inline-def" inline-def))

(defn deactivate
  [s]
  (ad/dispose-disposables!))

(defn before
  [done]
  (deactivate nil)
  (done)
  (activate nil)
  (aa/info-message "Reloaded Adorn"))
