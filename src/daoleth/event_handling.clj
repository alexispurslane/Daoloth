(ns daoleth.event-handling
  (:use seesaw.core))

(defn event-handler [e state]
  (case (config e :id)
    :event-new-level
    (println (show! (dialog :content (vertical-panel
                                      :items ["Level Name:"
                                              (text :id :level-name "unkown-level")]))))

    :event-open-level
    (println (show! (dialog :content (vertical-panel
                                      :items ["Level Name:"
                                              (text :id :level-name "unkown-level")]))))
    :event-save-level
    (do
      (swap! state assoc :saved? true)
      (println "Saved " (:filename @state)))

    :event-create-object
    (let [contents (vertical-panel :items ["Object Name:"
                                           (text :id :object-name "crate")
                                           "Object Character:"
                                           (text :id :object-char "=")])]
      (println (show! (dialog :content contents))))))
