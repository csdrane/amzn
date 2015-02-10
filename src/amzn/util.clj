(ns amzn.util
  (:require  [amzn.schema :as db])
  (:import (java.util.concurrent ScheduledThreadPoolExecutor TimeUnit)))

(defn stay-alive [m]
  (-> (ScheduledThreadPoolExecutor. 1)
      (.scheduleAtFixedRate db/ping-db 0 m TimeUnit/MINUTES)))
