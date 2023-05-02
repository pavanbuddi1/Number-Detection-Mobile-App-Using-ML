import flask
import werkzeug
import time
import os
from keras.models import load_model
from PIL import Image, ImageOps, ImageEnhance
import numpy as np
import matplotlib
import matplotlib.pyplot as plt
import cv2

#loading the prediction model 
model = load_model('mymodel.h5')

UPLOAD_FOLDER = './uploadedImages/'

app = flask.Flask(__name__)
app.config['UPLOAD_FOLDER'] = UPLOAD_FOLDER

@app.route('/', methods=['GET', 'POST'])
def handle_request():
    if flask.request.method == 'POST':
        #reading the image file from the POST request
        print("\n\n\n\n\nRecieved POST request....")
        imagefile = flask.request.files['image']

        #fetching the file name from the POST request
        filename = werkzeug.utils.secure_filename(imagefile.filename)
        print("\nReceived image file name : " + imagefile.filename)
        timestr = time.strftime("%Y%m%d_%H%M%S")
        print("Image recieved at: ", timestr)

        #predicting the test image
        print("\nOpening image file using PIL..")
        img = Image.open(imagefile)
        img2 = img
        print("Type of image (using PIL) = ", type(img))
        print('Resizing the image as per the prediction model..')
        img = img.resize((28, 28))
        img = img.transpose(Image.Transpose.ROTATE_270)
        print("Converting image to Grey Scale..")
        img = img.convert('L')
        img = ImageOps.invert(img)
        factor = 1.5 #increase contrast
        enhancer = ImageEnhance.Contrast(img)
        img = enhancer.enhance(factor)
        threshold = 125
        img = img.point( lambda p: p if p > threshold else 0 )
        img = np.array(img)
        print("Reshaping the image..")
        img= img.reshape(1, 28, 28, 1)
        img = img/255.0
        print("\nPredicting the image..\n")
        res = model.predict([img])[0]
        print("\nNumber predicted: " + str(np.argmax(res)) + " Accuracy: " + str(int(max(res)*100))+'%')

        number = str(np.argmax(res))
        path = os.path.join("./uploadedImages/" + number)
        if not os.path.exists(path):
            os.mkdir(path)  
        imagefile.seek(0)
        imagefile.save(os.path.join(path, timestr+'_'+filename))
        
        print("\nImage saved to ", str(path) + "/ successfully..!!\n\n\n\n\n")
        return "Image Uploaded Successfully"
    return '''
    <!doctype html>
    <title>Upload new File</title>
    <h1>Upload new File</h1>'''

if __name__ == "__main__":
    app.run(host="0.0.0.0", port=1122, debug=True)