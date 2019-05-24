(ns adorn.vscode
  (:refer-clojure :exclude [range])
  (:require
   [clojure.string :as cs]
   ["os" :as no]
   ["vscode" :as nv]))

(defn current-platform
  []
  (let [os-type (.type no)]
    (case os-type
      "Darwin" :darwin
      "Linux" :linux
      "Windows_NT" :win32)))

;; id is publisher.name where publisher and name come
;; from package.json -- however, if publisher is the
;; empty string, currently undefined_publisher is used :(
(defn extension-path
  [id]
  (->> id
    (.getExtension (.-extensions nv))
    .-extensionPath))

(defn register-command!
  [id f]
  (-> (.-commands nv)
      (.registerCommand id f)))

(defn register-text-editor-command!
  [id f]
  (-> (.-commands nv)
      (.registerTextEditorCommand id f)))

(defn push-subscription!
  [^js ctx disp]
  (-> (.-subscriptions ctx)
      (.push disp)))

(defn info-message
  [msg]
  (-> (.-window nv)
      (.showInformationMessage msg)))

(defn warning-message
  [msg]
  (-> (.-window nv)
      (.showWarningMessage msg)))

(defn get-config
  ([path]
   (-> (.-workspace nv)
       (.getConfiguration path)))
  ([conf key]
   (.get conf key)))

(defn create-terminal
  [name]
  (-> (.-window nv)
      (.createTerminal name)))

(defn on-did-open-terminal
  [cb]
  (-> (.-window nv)
      (.onDidOpenTerminal cb)))

(defn new-output-channel
  [name]
  (-> (.-window nv)
      (.createOutputChannel name)))

(defn current-editor
  []
  (-> (.-window nv)
      .-activeTextEditor))

;; (defn current-document
;;   ([]
;;    (current-document (current-editor)))
;;   ([^js editor]
;;    (.-document editor)))

(defn current-line
  ([]
   (current-line (current-editor)))
  ([^js editor]
   (-> (.-document editor)
       (.lineAt (-> editor
                    .-selection
                    .-start))
       (.-text))))

(defn current-selection
  ([]
   (current-selection (current-editor)))
  ([^js editor]
   (-> (.-document editor)
       (.getText (.-selection editor)))))

(defn current-position
  ([]
   (current-position (current-editor)))
  ([^js editor]
   (-> (.-selection editor)
       .-active)))

(defn current-row
  ([]
   (current-row (current-editor)))
  ([^js editor]
   (.-line (current-position editor))))

(defn current-col
  ([]
   (current-col (current-editor)))
  ([^js editor]
   (.-character (current-position editor))))

;; XXX: naming...
(defn current-buffer
  ([]
   (current-buffer (current-editor)))
  ([^js editor]
   (-> (.-document editor)
       .getText)))

(defn current-path
  ([]
   (current-path (current-editor)))
  ([^js editor]
   (-> (.-document editor)
       .-fileName)))

(defn position
  [r c]
  (nv/Position. r c))

(defn range
  ([p1 p2]
   (nv/Range. p1 p2))
  ([r1 c1 r2 c2]
   (nv/Range.
    (position r1 c1)
    (position r2 c2))))
