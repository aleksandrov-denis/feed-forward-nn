package nn.DenisAleksandrov;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Scanner;

public class Network {
    /**
     * Signifies the number layers and number of neurons in each layer
     */
    public final int[] LAYER_SIZES;
    /**
     * The number of neurons in the input layer
     */
    public final int INPUT_SIZE;
    /**
     * The number of neurons in the output layer
     */
    public final int OUTPUT_SIZE;
    /**
     * The number of layers in the network
     */
    public final int NETWORK_SIZE;

    /**
     * neurons: a 2D matrix of neurons in the network
     * (first parameter): the layer we are looking at
     * (second parameter): particular neuron to look at
     */
    public Neuron[][] neurons;

    /**
     * Mean Squared Error SUM((output - target)^2)/OUTPUT_SIZE
     */
    private double MSE;

    /**
     * Constructor for the network
     * Initializes the neuron matrix
     * Fills the weights in between neurons with random doubles from -1 to 1
     * Fills the bias of each neuron with random  doubles from -1 to 1
     * @pre layerSizes != null
     * @param layerSizes array signifying the number of neurons at each layer
     */
    protected Network(int[] layerSizes){
        assert layerSizes != null;
        LAYER_SIZES = layerSizes;
        INPUT_SIZE = LAYER_SIZES[0];
        NETWORK_SIZE = LAYER_SIZES.length;
        OUTPUT_SIZE = LAYER_SIZES[NETWORK_SIZE - 1];

        neurons = new Neuron[NETWORK_SIZE][];

        for(int layer = 0; layer < NETWORK_SIZE; layer++){
            neurons[layer] = new Neuron[LAYER_SIZES[layer]];
            for(int neuron = 0; neuron < LAYER_SIZES[layer]; neuron++){
                neurons[layer][neuron] = new Neuron();
            }
        }

        for(int layer = 1; layer < NETWORK_SIZE; layer++){
            for (int neuron = 0; neuron < LAYER_SIZES[layer]; neuron++) {
                neurons[layer][neuron].weight = new double[LAYER_SIZES[layer - 1]];
                for (int prevNeuron = 0; prevNeuron < LAYER_SIZES[layer - 1]; prevNeuron++) {
                    neurons[layer][neuron].weight[prevNeuron] = 2 * Math.random() - 1;
                }
                neurons[layer][neuron].bias = 2 * Math.random() - 1;
            }
        }
    }

    /**
     * Feeds forward through the network, updates output layer neurons' output values based on weights and biases
     * @param input value array for the neurons in input layer
     * @return array of the output layer neurons post feeding the input through the network
     */
    public Neuron[] feedForward(double[] input){
        if(input.length != INPUT_SIZE) return null;
        //neurons[0] = input;
        for(int i = 0; i< input.length; i++){
            neurons[0][i].aOutput = input[i];
        }
        for(int layer = 1; layer < NETWORK_SIZE; layer++){
            for(int neuron = 0; neuron < LAYER_SIZES[layer]; neuron++){
                double sum = 0;
                for(int prevNeuron = 0; prevNeuron < LAYER_SIZES[layer-1]; prevNeuron++){
                    sum += neurons[layer-1][prevNeuron].aOutput * neurons[layer][neuron].weight[prevNeuron];
                }
                sum += neurons[layer][neuron].bias;
                neurons[layer][neuron].aOutput = sigmoid(sum);
                neurons[layer][neuron].derivative = neurons[layer][neuron].aOutput * (1 - neurons[layer][neuron].aOutput);
            }
        }
        return neurons[NETWORK_SIZE-1];
    }

    /**
     *
     * @param input given data vector
     * @param target target vector
     * @param eta learning rate
     */
    protected void train(double[] input, double[] target, double eta){
        if(input.length != INPUT_SIZE || target.length != OUTPUT_SIZE) return;
        feedForward(input);
        backprop(target);
        updateWeightsBiases(eta);
    }

    /**
     * Back propagation algorithm for the network
     * Output layer neurons have an error signal of ((output - target) * output_derivative)
     * Hidden layer neurons have an error signal of (SUM(weight * backprop_error) * derivative)
     * @pre target != null
     * @param target target values to compare output neuron outputs to
     */
    private void backprop(double[] target){
        assert target != null;
        double mseSum = 0;
        for(int neuron = 0; neuron < LAYER_SIZES[NETWORK_SIZE-1]; neuron++){
            neurons[NETWORK_SIZE-1][neuron].backprop_error =
                    (neurons[NETWORK_SIZE-1][neuron].aOutput - target[neuron])
                            * neurons[NETWORK_SIZE-1][neuron].derivative;
            mseSum += Math.pow((neurons[NETWORK_SIZE-1][neuron].aOutput - target[neuron]), 2);
        }
        for(int layer = NETWORK_SIZE-2; layer > 0; layer--){
            for(int leftNeuron = 0; leftNeuron < LAYER_SIZES[layer]; leftNeuron++){
                double sum = 0;
                for(int rightNeuron = 0; rightNeuron < LAYER_SIZES[layer+1]; rightNeuron++){
                    sum += neurons[layer+1][rightNeuron].weight[leftNeuron] * neurons[layer+1][rightNeuron].backprop_error;
                }
                neurons[layer][leftNeuron].backprop_error = sum * neurons[layer][leftNeuron].derivative;
            }
        }
        MSE = mseSum / OUTPUT_SIZE;
    }

    /**
     * Updates weights and biases on each neuron by a factor of eta
     * Note: eta could be made to change dynamically with the error
     * @param eta learning rate by a factor of which to update the biases and weights
     */
    private void updateWeightsBiases(double eta){
        for(int layer = 1; layer < NETWORK_SIZE; layer++){
            for(int neuron = 0; neuron < LAYER_SIZES[layer]; neuron++){
                double delta = - eta * neurons[layer][neuron].backprop_error;
                neurons[layer][neuron].bias += delta;
                for(int prevNeuron = 0; prevNeuron < LAYER_SIZES[layer-1]; prevNeuron++){
                    neurons[layer][neuron].weight[prevNeuron] += delta * neurons[layer-1][prevNeuron].aOutput;
                }
            }
        }
    }

    /**
     * Sigmoid function to normalize the sum of incoming signals to each neuron to a 0-1 range
     * @param x the sum of incoming signals
     * @return normalized (0-1) value of the sum
     */
    private double sigmoid(double x){
        return 1d / (1 + Math.exp(-x));
    }

    public double getMSE(){
        return MSE;
    }

    /**
     * Saves the current state of the Network.
     *
     * File format:
     * int[] LAYER_SIZES
     * Neuron[][] neurons
     *          Format for saving each neuron:
     *          double aOutput
     *          double[] weight
     *          double backprop_error
     *          double derivative
     *          double bias
     * double MSE
     *
     * @param pathname where to save the current state of the Network
     * @throws IOException in the case the file cannot be created
     */
    public void saveNetwork(String pathname) throws IOException {
        File file = new File(pathname);
        FileWriter fw = new FileWriter(file);


        // Recording int[] LAYER_SIZES
        for(int i = 0; i < this.LAYER_SIZES.length; i++){
            if(i == this.LAYER_SIZES.length - 1){
                fw.write(this.LAYER_SIZES[i] + "\n");
                continue;
            }
            fw.write(this.LAYER_SIZES[i] + ", ");
        }

        // Recording Neuron[][] neurons
        for(int layer = 0; layer < this.neurons.length; layer++){
            for(int neuron = 0; neuron < this.neurons[layer].length; neuron++){

                // Recording double aOutput
                fw.write((Double) this.neurons[layer][neuron].aOutput + "\n");

                // Recording double[] weight
                if(layer > 0) {
                    int weightLength = this.neurons[layer][neuron].weight.length;
                    for (int prevNeuron = 0; prevNeuron < weightLength; prevNeuron++) {
                        if (prevNeuron == weightLength - 1) {
                            fw.write((Double) this.neurons[layer][neuron].weight[prevNeuron] + "\n");
                            continue;
                        }
                        fw.write((Double) this.neurons[layer][neuron].weight[prevNeuron] + ", ");
                    }
                }
                else{
                    fw.write("\n");
                }

                // Recording double backprop_error
                fw.write((Double) this.neurons[layer][neuron].backprop_error + "\n");

                // Recording double derivative
                fw.write((Double) this.neurons[layer][neuron].derivative + "\n");

                // Recording double bias
                fw.write((Double) this.neurons[layer][neuron].bias + "\n");
            }
        }

        // Recording double MSE
        fw.write((Double) this.MSE + "\n");

        fw.close();
    }

    /**
     * Loads a Network from a specifically formatted file. Check the documentation of the saveNetwork
     * method for formatting details.
     * @param pathname from where to load a Network
     * @return loaded Network
     * @throws IOException in the case that the specified file cannot be opened
     */
    public static Network loadNetwork(String pathname) throws IOException{
        Scanner sc = new Scanner(new File(pathname));

        // Loading the int[] LAYER_SIZES from file. Network gets instantiated with LAYER_SIZES
        // Constructor handles the recovery of the states of INPUT_SIZE, OUTPUT_SIZE, NETWORK_SIZE
        Network loadedNetwork = new Network(Arrays.stream(sc.nextLine().split(", ")).
                mapToInt(Integer::parseInt).toArray());

        // Loads the neurons into the network
        for(int layer = 0; layer < loadedNetwork.neurons.length; layer++){
            for(int neuron = 0; neuron < loadedNetwork.neurons[layer].length; neuron++){

                // Loads the output for each neuron
                loadedNetwork.neurons[layer][neuron].aOutput = Double.parseDouble(sc.nextLine());

                // Loads the weights between the current neuron and all neurons in the previous layer for each neuron
                String line = sc.nextLine();
                if(!line.equals("")) {
                    loadedNetwork.neurons[layer][neuron].weight = Arrays.stream(line.split(", ")).
                            mapToDouble(Double::parseDouble).toArray();
                }

                // Loads the backpropagation error for each neuron
                loadedNetwork.neurons[layer][neuron].backprop_error = Double.parseDouble(sc.nextLine());

                // Loads the derivative for each neuron
                loadedNetwork.neurons[layer][neuron].derivative = Double.parseDouble(sc.nextLine());

                // Loads the bias for each neuron
                loadedNetwork.neurons[layer][neuron].bias = Double.parseDouble(sc.nextLine());
            }
        }
        loadedNetwork.MSE = Double.parseDouble(sc.nextLine());
        sc.close();
        return loadedNetwork;
    }

    /**
     * Uses the output layer of a trained Network to give an interpretation of the sensor data.
     * Currently accounts only for distance sensors.
     * Each sensor data value from the input layer corresponds to three output values.
     * e.g. LEFT_DISTANCE --> 1. FAR  2. NEAR  3. BLOCKING
     * Considering that the Network is properly trained, one of the three values will be above the threshold.
     * In which case a message will be recorded at output_array[0] stating that the robot
     * is either far, near, or being blocked by some obstacle.
     * @param threshold some confidence value
     * @return array of messages informing the KNN of the current state of events regarding distance
     */
    public String[] getMessage(double threshold){
        String[] out = new String[4];
        String left = "OBSTACLE-LEFT UNKNOWN";
        String forward = "OBSTACLE-FORWARD UNKNOWN";
        String right = "OBSTACLE-RIGHT UNKNOWN";
        String back = "OBSTACLE-BACK UNKNOWN";
        Neuron[] n = this.neurons[NETWORK_SIZE - 1];
        for(int neuron = 0; neuron < n.length; neuron++){
            if(n[neuron].aOutput >= threshold){
                if(neuron < 3){
                    switch(neuron){
                        case 0:
                            left = "OBSTACLE-LEFT FAR";
                            break;
                        case 1:
                            left = "OBSTACLE-LEFT NEAR";
                            break;
                        case 2:
                            left = "OBSTACLE-LEFT BLOCKING";
                            break;
                    }
                    out[0] = left;
                }
                else if(neuron < 6){
                    switch(neuron){
                        case 3:
                            forward = "OBSTACLE-FORWARD FAR";
                            break;
                        case 4:
                            forward = "OBSTACLE-FORWARD NEAR";
                            break;
                        case 5:
                            forward = "OBSTACLE-FORWARD BLOCKING";
                            break;
                    }
                    out[1] = forward;
                }
                else if(neuron < 9){
                    switch(neuron){
                        case 6:
                            right = "OBSTACLE-RIGHT FAR";
                            break;
                        case 7:
                            right = "OBSTACLE-RIGHT NEAR";
                            break;
                        case 8:
                            right = "OBSTACLE-RIGHT BLOCKING";
                            break;
                    }
                    out[2] = right;
                }
                else if(neuron < 12){
                    System.out.println("Inside back-sensor interpreter");
                    switch(neuron){
                        case 9:
                            back = "OBSTACLE-BACK FAR";
                            break;
                        case 10:
                            back = "OBSTACLE-BACK NEAR";
                            break;
                        case 11:
                            back = "OBSTACLE-BACK BLOCKING";
                            break;
                    }
                    out[3] = back;
                }
            }
        }
        return out;
    }

    public String[] think(){

        return getMessage(0.98);
    }


    static public class Neuron{
        /**
         * Output value of the neuron
         */
        public double aOutput;
        /**
         * Weight between this neuron and all previous neurons connected to it
         */
        private double[] weight;
        /**
         * Error between actual and target values for each output
         */
        private double backprop_error;
        /**
         * Sigmoid derivative for each neuron (output * (1 - output))
         */
        private double derivative;
        /**
         * Given bias to be used in the summation function per output
         */
        private double bias;
    }

    /**
     * Shows some ways to use this class.
     */
    public static void main(String[] args) throws IOException {
        long startTime = System.nanoTime();
        int[] t = new int[]{4,4,4,4};
        Network n = new Network(t);

        double[] input = new double[]{50,10,30,20};
        double[] target = new double[]{0,1,0,0};


        for(int i = 0; i<100000; i++){
            n.train(input, target, 0.3);
        }

        Neuron[] test = n.feedForward(input);
        double[] o = new double[test.length];
        for(int i = 0; i < test.length; i++){
            o[i] = test[i].aOutput;
        }
        System.out.println("Actual Output: " + Arrays.toString(o));
        long endTime = System.nanoTime();
        System.out.println("Running Time: " + (endTime - startTime));
        n.saveNetwork("save_network_test.txt");
        Network net = Network.loadNetwork("save_network_test.txt");
        test = net.feedForward(input);
        o = new double[test.length];
        for(int i = 0; i < test.length; i++){
            o[i] = test[i].aOutput;
        }
        System.out.println("Actual Output Loaded: " + Arrays.toString(o));
    }
}
