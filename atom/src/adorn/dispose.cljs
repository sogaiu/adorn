(ns adorn.dispose)

(def CompositeDisposable
  (.-CompositeDisposable (js/require "atom")))

(def disposables
  (atom (CompositeDisposable.)))

(defn dispose-disposables!
  []
  (.dispose ^js @disposables))

(defn reset-disposables!
  []
  (reset! disposables (CompositeDisposable.)))

(defn command-for
  [name f]
  (let [disp (-> (.-commands js/atom)
                 (.add "atom-text-editor"
                       (str "adorn:" name)
                       f))]
    (.add @disposables disp)))
