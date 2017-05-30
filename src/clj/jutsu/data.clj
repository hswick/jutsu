(ns jutsu.data
  (:import [org.datavec.api.util ClassPathResource]
           [org.datavec.api.records.reader.impl.csv CSVRecordReader]
           [org.datavec.api.split FileSplit]
           [org.deeplearning4j.datasets.datavec RecordReaderDataSetIterator]
           [org.nd4j.linalg.factory Nd4j]))
           
(defn absolute-path [filename]
  (-> (ClassPathResource. filename)
      (.getFile)
      (.getAbsolutePath)))

;;Can only iterate over once
(defn csv-reader [filename];;default args
  ;num lines skip, delimiter;
  (let [reader (CSVRecordReader. 0 ",")]
    (.initialize reader (FileSplit. (clojure.java.io/file (absolute-path filename))))
    reader))

;;Options to string json (array of arrays)
(defn till-no-next [nextable]
  (loop [data []]
    (if (not (.hasNext nextable))
      data
      (recur (conj data
               (seq (.next nextable)))))))

;;Gets records of strings
(defn line-records [data]
  (map (fn [row] (map #(String. (.getBytes %)) row)) data))

;;Returns a lazyseq of edn data
;;Assumes there is a header
;:Returns a map of header and data
(defn csv->clj [filename]
  (-> (csv-reader filename)
      (till-no-next)
      (line-records)))

(defn rows [coll]
  (if (seq? (first coll)) (count coll) 1))

(defn cols [coll]
  (if (seq? (first coll)) (count (first coll)) (count coll)))

(defn nd4j->clj [nd4j-array]
  (if (= 1 (first (.shape nd4j-array)))
    (into [] nd4j-array)
    (map #(into [] %) nd4j-array)))

;;Add good exceptions to this
(defn strings->floats [string-seq]
  (map #(Float/parseFloat %) string-seq))

;;Currently supports 1 and 2d arrays
(defn clj->nd4j [coll]
  (let [h (rows coll) w (cols coll)]
    (if (= h 1)
      (Nd4j/create (float-array (seq coll)))
      (let [new-array (Nd4j/create h w)]
        (doseq [i (range 0 h)]
          (.putRow new-array i (Nd4j/create (float-array (seq (nth coll i))))))
        new-array))))  
