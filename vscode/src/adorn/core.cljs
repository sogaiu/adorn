(ns adorn.core
  (:require
   [adorn.jsonrpc :as aj]
   [adorn.vscode :as av]
   ["child_process" :as ncp]))

(defn adorn-path
  []
  ;; XXX: modify if publisher / name changes in package.json
  (str (av/extension-path "undefined_publisher.vscode-adorn")
     "/bin/adorn"))

(defn adorn-defn
  [^js editor fn-name]
  (av/info-message "invoking helper...")
  (let [p (.spawn ncp (adorn-path) #js [] #js {})
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
            ;; row, col values need to be one less for vscode api
            (let [[[start-row start-col] [end-row end-col]] text-range]
              (.edit (av/current-editor)
                (fn [eb]
                  (.replace eb
                    (av/range (dec start-row) (dec start-col)
                              (dec end-row) (dec end-col))
                    text))))))))
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
      (aj/to-str fn-name
        ;; buffer text, helper counts from 1 for both row and column values
        [(av/current-buffer) (inc (av/current-row)) (inc (av/current-col))]))))

(defn inlinedef
  [^js editor]
  (adorn-defn editor "inlinedef"))

(defn tapargs
  [^js editor]
  (adorn-defn editor "tapargs"))

(defn activate
  [context]
  (.log js/console "activate called")
  (let [disp-1 (av/register-text-editor-command!
                "adorn.inlinedef"
                #'inlinedef)
        _ (av/push-subscription! context disp-1)
        disp-2 (av/register-text-editor-command!
                "adorn.tapargs"
                #'tapargs)
        _ (av/push-subscription! context disp-2)]
    true))

(defn deactivate
  [])
