(ns adorn.atom)

(defn current-platform
  []
  (let [os-type (.type (js/require "os"))]
    (case os-type
      "Darwin" :darwin
      "Linux" :linux
      "Windows_NT" :win32)))

;; thanks to atom-packages-dependencies
(defn path-for-pkg
  [pkg-name]
  (->> pkg-name
       (.getLoadedPackage (.-packages js/atom))
       (.-mainModulePath)))

(defn current-workspace
  []
  (.getView (.-views js/atom)
            (.-workspace js/atom)))

(defn warn-message
  ([title]
   (warn-message title ""))
  ([title text]
   (-> (.-notifications js/atom)
       (.addWarning title #js {:detail text}))))

(defn error-message
  ([title]
   (error-message title ""))
  ([title text]
   (-> (.-notifications js/atom)
       (.addError title #js {:detail text}))))

(defn info-message
  ([title]
   (info-message title ""))
  ([title text]
   (-> (.-notifications js/atom)
       (.addInfo title #js {:detail text}))))

(defn current-editor []
  (-> (.-workspace js/atom)
      .getActiveTextEditor))

(defn current-pos
  ([]
   (current-pos (current-editor)))
  ([^js editor]
   (let [point (.getCursorBufferPosition editor)]
     [(.-row point) (.-column point)])))

(defn current-row
  ([]
   (current-row (current-editor)))
  ([^js editor]
   (let [[row _] (current-pos editor)]
     row)))

(defn line-at
  ([line-no]
   (line-at (current-editor) line-no))
  ([^js editor line-no]
   (.lineTextForBufferRow editor line-no)))

(defn current-line
  ([]
   (current-line (current-editor)))
  ([^js editor]
   (.lineTextForBufferRow editor (current-row editor))))

(defn current-col
  ([]
   (current-col (current-editor)))
  ([^js editor]
   (let [[_ col] (current-pos editor)]
     col)))

(defn current-selection
  ([]
   (current-selection (current-editor)))
  ([^js editor]
   (.getSelectedText editor)))

(defn current-buffer
  ([]
   (current-buffer (current-editor)))
  ([^js editor]
   (.getText editor)))

(defn current-path
  ([]
   (current-path (current-editor)))
  ([^js editor]
   (.getPath editor)))

