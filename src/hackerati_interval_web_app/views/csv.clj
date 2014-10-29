(ns hackerati-interval-web-app.views.csv
  (:require [clojure.data.csv :as csv] 
            [hackerati-interval-web-app.schema :as db])
  (:import java.io.StringWriter))

(defn chart-csv [productid]
  "Serves CSV for use in chart."
  (let [data (db/get-prices productid)
        columns [:date :price]
        headers (map name columns)
        rows (mapv #(mapv % columns) data)]
    (with-open [s (StringWriter.)]
      (csv/write-csv s (cons headers rows))
      (str s))))

