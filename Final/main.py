import base64
import os, os.path
from time import strftime
import cv2

import flask
from flask import Flask, request, make_response, jsonify
from datetime import date

import werkzeug
import time
import os
from keras.models import load_model
from PIL import Image, ImageOps, ImageEnhance
import numpy as np
import matplotlib
import matplotlib.pyplot as plt
import cv2
from io import BytesIO

#loading the prediction model 
model = load_model('mymodel.h5')

# Create a flask instance
app = Flask(__name__)


@app.route('/', methods=['POST'])
def upload_image():
    imagestring = flask.request.form['encodedImage']
    # category = flask.request.form['category']
    image = base64.b64decode(imagestring)
    imagefileName = "IMG" + ".jpg"

    with safe_open_path("./New" + "/" + imagefileName) as f:
        f.write(image)
    f.close()

    print("\nOpening image file using PIL..")
    img = Image.open(BytesIO(base64.b64decode(imagestring)))
    img2 = img
    print("Type of image (using PIL) = ", type(img))
    print('Resizing the image as per the prediction model..')
    img = img.resize((28, 28))
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
    res_str = str(np.argmax(res))
    accu_str = str(int(max(res)*100))
    #return res_str+','+accu_str
    data = {'accuracy':accu_str, 'number':res_str}
    return make_response(jsonify(data), 201)


def safe_open_path(path):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    return open(path, 'wb')


if __name__ == '__main__':
    # app.run()
    app.run("0.0.0.0", port=5000, debug=True)

# See PyCharm help at https://www.jetbrains.com/help/pycharm/
