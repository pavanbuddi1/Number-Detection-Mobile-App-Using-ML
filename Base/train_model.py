#Importing required modules for the model.. 
from tensorflow.keras.datasets import mnist
from tensorflow.keras.models import Sequential
from tensorflow.keras.layers import Dense, Dropout, Conv2D, Flatten, MaxPooling2D
from tensorflow.keras.utils import to_categorical

#import data from MNIST model
(training_data_x, training_data_y), (testing_data_x, testing_data_y) = mnist.load_data()
training_data_x = training_data_x.reshape((training_data_x.shape[0], 28, 28, 1)).astype('float32')
testing_data_x = testing_data_x.reshape((testing_data_x.shape[0], 28, 28, 1)).astype('float32')
#scaling down the inputs
training_data_x = training_data_x / 255
testing_data_x = testing_data_x / 255
training_data_y = to_categorical(training_data_y)
testing_data_y = to_categorical(testing_data_y)
num_classes = testing_data_y.shape[1]

#creating CNN model function
def create_model():
	model = Sequential()
	model.add(Conv2D(30, (5, 5), input_shape=(28, 28, 1), activation='relu'))
	model.add(MaxPooling2D())
	model.add(Conv2D(15, (3, 3), activation='relu'))
	model.add(MaxPooling2D())
	model.add(Dropout(0.2))
	model.add(Flatten())
	model.add(Dense(128, activation='relu'))
	model.add(Dense(50, activation='relu'))
	model.add(Dense(num_classes, activation='softmax'))
	# Compile model
	model.compile(loss='categorical_crossentropy', optimizer='adam', metrics=['accuracy'])
	return model

#calling create model fucntion
model = create_model()

#save the model..
hist = model.fit(training_data_x, training_data_y, validation_data=(testing_data_x, testing_data_y), epochs=10, batch_size=200)
model.save('mymodel.h5')

#test the model
scores = model.evaluate(testing_data_x, testing_data_y, verbose=0)
print("Model err_percentage: %.1f%%" % (100-scores[1]*100))