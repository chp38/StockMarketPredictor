package chp38.WEKA;

import weka.classifiers.bayes.NaiveBayes;
import weka.core.Instances;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class WekaHandler {

    /**
     * Variable to hold the J48 tree
     */
    private NaiveBayes tree;

    /**
     * Method to build the J48 tree with a given training dataset
     *
     * @throws Exception
     */
    public void buildModel() throws Exception {
        Instances data = this.getInstances("labelled.arff");

        data.setClassIndex(data.numAttributes() - 1);

        String[] options = new String[1];
        options[0] = "";
        this.tree = new NaiveBayes();
        tree.setOptions(options);
        tree.buildClassifier(data);

    }

    /**
     * Methos to classify new data, based on the built tree from the
     * training data
     *
     * @throws Exception
     */
    public void classifyData() throws Exception {
        Instances test = this.getInstances("unlabelled.arff");

        test.setClassIndex(test.numAttributes() - 1);

        for (int i = 0; i < test.numInstances(); i++) {
            double clsLabel = this.tree.classifyInstance(test.instance(i));
            test.instance(i).setClassValue(clsLabel);
            System.out.println(clsLabel);
        }
    }

    /**
     * Method to return the instances from the .arff files
     *
     * @param file
     * @return Instances the instances from the arff file
     * @throws IOException
     */
    public Instances getInstances(String file) throws IOException {
        BufferedReader reader = new BufferedReader(
                new FileReader(file));

        Instances data = new Instances(reader);

        reader.close();

        return data;
    }
}