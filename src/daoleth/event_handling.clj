(ns daoleth.event-handling
  (:use seesaw.core)
  (:require [daoleth.constants :as dc]))

(defn get-state-from-file [filename]
  (->> filename slurp read-string))

(defn write-state-to-file [state]
  (spit (:filename state) (with-out-str
                            (-> state
                                (select-keys [:map :size :filename :objects :painting-object])
                                pr))))

(defn make-dialog [& items]
  (dialog
   :type :question
   :success-fn #(map text (select % [:<javax.swing.JTextField>]))
   :option-type :ok-cancel
   :content (vertical-panel :items items)))

(defn event-handler [e state]
  (case (config e :id)
    :event-new-level
    (let [results (show! (make-dialog "Level Name:"
                                      (text :text "unkown-level" :id :level-name)))]
      (reset! state dc/initial-state)
      (dc/create-initial-map state)
      (swap! state assoc :filename (first results))
      (swap! state assoc :new-file? true)
      (println @state))

    :event-open-level
    (let [results (show! (make-dialog "Level Name:"
                                      (text :text "unkown-level" :id :level-name)))]
      (reset! state (get-state-from-file (first results)))
      (swap! state assoc :new-file? true)
      (println @state))

    :event-save-level
    (do
      (swap! state assoc :saved? true)
      (write-state-to-file @state))

    :event-create-object
    (let [results (show! (make-dialog "Object Name:"
                                      (text :text "crate" :id :object-name)
                                      "Object Character:"
                                      (text :text "=" :id :object-char)))
          object [(keyword (first results)) (first (second results))]]
      (swap! state assoc :objects (conj (:objects @state) object)))))
