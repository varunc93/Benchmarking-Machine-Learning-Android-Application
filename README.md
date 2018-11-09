# Benchmarking-Machine-Learning-Android-Application
Android application developed during Mobile Computing Course at ASU

In this project, we worked toward testing these machine learning capabilities on a mobile phone CPU. We implemented training of machine learning algorithms on the server end and tested the trained models on the mobile end. 
 
At first, we carried out training of a classifier in the mobile CPU itself using WEKA libraries in the android device. We were able to train a single classifier in the mobile CPU. Since, it was not very efficient, we went ahead with the approach to carry out training for multiple classifiers on the server. 
 
An android application was developed for the project, which took the list of classifiers from the user as an input. Other than that, the users must input training parameters for each of the models. As per the parameters inputted, a java command is created that will execute the weka machine learning libraries to train the selected model. 
 
After the user inputs parameters for each of the selected models, the commands along with the training data file is sent to the server for training purposes. Weka libraries are used to train the models and return the trained model files back to the mobile device. 
 
 
