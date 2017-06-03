(ns jutsu.data
  (:import [org.datavec.api.util ClassPathResource]
           [org.datavec.api.records.reader.impl.csv CSVRecordReader]
           [org.datavec.api.split FileSplit]
           [org.deeplearning4j.datasets.datavec RecordReaderDataSetIterator]
           [org.nd4j.linalg.factory Nd4j]
           [org.nd4j.linalg.ops.transforms Transforms]))

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

;;Grabbing first value, this isnt correct
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

(defn mean [ndarray]
  (let [shape (.shape ndarray)
        nrows (first shape)
        ncols (second shape)
        new-array (Nd4j/zeros 1 ncols)]
    (doseq [i (range nrows)]
      (.addi new-array (.getRow ndarray i)))
    new-array))

(defn covariance [ndarray]
    (let [average (mean ndarray)
          shape (.shape ndarray)
          sum (Nd4j/zeros (second shape) (second shape))]
      (doseq [i (range 0 (first shape))]
        (let [variance (.sub (.getRow ndarray i) average)
              row-covar (.mmul (.transpose (.dup variance)) variance)]
          (.addi sum row-covar)))         
      (.div sum (first shape))))

(defn svd-decomp [ndarray]
  (let [shape (.shape ndarray)
        rows (first shape)
        cols (second shape)
        s (Nd4j/create (if (< rows cols) rows cols))
        vt (Nd4j/create cols cols)]
    (.sgesvd (.lapack (Nd4j/getBlasWrapper))
      ndarray s nil vt)
    {:singularvalues s :eigenvectors_transposed vt}))

;;Stacks a collection of ndarrays vertically (by row)
(defn vstack-arrays [ndarrays]
  (let [shape (.shape (first ndarrays))
        new-array (Nd4j/create (count ndarrays) (second shape))]
    (doseq [n (range 0 (count ndarrays))]
      (.putRow new-array n (nth ndarrays n)))
    new-array))

;;Stacks a collection of ndarrays horizontally
(defn hstack-arrays [ndarrays]
  (let [shape (.shape (first ndarrays))
        new-array (Nd4j/create (first shape) (count ndarrays))]
    (doseq [n (range 0 (count ndarrays))]
      (.putColumn new-array n (nth ndarrays n)))
    new-array))
  
(defn pca [ndarray num-dims]
  (let [covar (covariance ndarray)
        svd-comps (svd-decomp covar)
        factors (->> (map-indexed (fn [i n] [n i]) (:singularvalues svd-comps))
                     (sort-by first)
                     reverse
                     (take num-dims)
                     (map (fn [[eigenvalue id]] (.getRow (:eigenvectors_transposed svd-comps) id)))
                     vstack-arrays)]
    (.mmul ndarray (.transpose factors))))

(defn normalize-zero [ndarray]
  (let [mn (Nd4j/mean ndarray 0)]
    (.subiRowVector ndarray mn)
    ndarray))

(defn normalize! [ndarray]
  (Transforms/normalizeZeroMeanAndUnitVariance ndarray))
