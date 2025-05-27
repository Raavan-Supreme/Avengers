package com.example.demo;

import ai.djl.Application;
import ai.djl.ModelException;
import ai.djl.inference.Predictor;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.ImageFactory;
import ai.djl.modality.cv.output.DetectedObjects;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ModelZoo;
import ai.djl.translate.TranslateException;

import java.io.IOException;
import java.nio.file.Path;

public class DjlOcrUtil {

    public static String extractText(Path imagePath) throws ModelException, TranslateException, IOException {
        Criteria<Image, String> criteria = Criteria.builder()
                .optApplication(Application.CV.ANY)
                .setTypes(Image.class, String.class)
                .build();

        try (var model = ModelZoo.loadModel(criteria);
             Predictor<Image, String> predictor = model.newPredictor()) {
            Image img = ImageFactory.getInstance().fromFile(imagePath);
            return predictor.predict(img);
        }
    }
}
