(ns medegas.core
  (:require [meinside.clogram :as cg]

            [medegas.handlers :as handlers]
            [medegas.database :as db])
  (:gen-class))

(def token (System/getenv "TOKEN_BOT"))

(def bot (cg/new-bot token :verbose? false #_true))
(def interval 1)

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!")
  (db/create-db)
  (db/tx-schema)
  (.addShutdownHook (Runtime/getRuntime)
                    (Thread. #(do
                                (println ">>> terminating application...")
                                (cg/stop-polling-updates bot))))
  (cg/poll-updates bot interval handlers/handle-bot))

#_(-main)
