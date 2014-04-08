/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.tul.dic.gui;

import cz.tul.dic.data.roi.CircularROI;
import cz.tul.dic.data.roi.ROI;
import cz.tul.dic.data.roi.RectangleROI;
import cz.tul.dic.data.task.TaskContainer;
import java.net.URL;
import java.util.HashSet;
import java.util.Iterator;
import java.util.ResourceBundle;
import java.util.Set;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;

/**
 *
 * @author Petr Jecmen
 */
public class InputPresenter extends ScrollPane implements Initializable, ChangeListener<String> {

    protected int imageIndex;
    protected Set<Shape> rois;
    private StringProperty imageIndexProperty;
    protected ImageView image;

    public boolean nextImage() {
        final boolean result = changeIndex(1);
        displayImage();
        return result;
    }

    protected boolean changeIndex(int change) {
        boolean result = false;
        imageIndex += change;

        if (imageIndex < 0) {
            imageIndex = Context.getInstance().getTc().getImages().size() - 1;
            result = true;
        } else if (imageIndex >= Context.getInstance().getTc().getImages().size()) {
            imageIndex = 0;
            result = true;
        }

        if (imageIndexProperty != null) {
            imageIndexProperty.setValue(Integer.toString(imageIndex));
        }
        return result;
    }

    public void displayImage() {
        final TaskContainer tc = Context.getInstance().getTc();
        final cz.tul.dic.data.Image i = tc.getImage(imageIndex);
        final Image img = SwingFXUtils.toFXImage(i, null);

        image.setImage(img);
        image.setFitWidth(img.getWidth());
        image.setFitHeight(img.getHeight());

        setContent(null);
        setContent(image);
        
        loadRois();
    }

    void loadRois() {
        rois.clear();
        final TaskContainer tc = Context.getInstance().getTc();
        final Set<ROI> taskRois = tc.getRois(imageIndex);
        Shape s = null;
        if (taskRois != null) {
            for (ROI r : taskRois) {
                if (r instanceof RectangleROI) {
                    s = new Rectangle(r.getX1(), r.getY1(), r.getWidth(), r.getHeight());
                } else if (r instanceof CircularROI) {
                    final CircularROI c = (CircularROI) r;
                    s = new Circle(c.getCenterX(), c.getCenterY(), c.getRadius());
                }
                if (s != null) {
                    rois.add(s);
                    s.setFill(new Color(1, 1, 1, 0));
                    s.setStroke(new Color(1, 1, 1, 1));
                }
            }

            final Iterator<Node> it = getChildren().iterator();
            while (it.hasNext()) {
                if (it.next() instanceof Shape) {
                    it.remove();
                }
            }
            getChildren().addAll(rois);
        }
    }

    public boolean previousImage() {
        final boolean result = changeIndex(-1);
        displayImage();
        return result;
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        rois = new HashSet<>();
        imageIndex = 0;

        setVbarPolicy(ScrollBarPolicy.AS_NEEDED);
        setHbarPolicy(ScrollBarPolicy.AS_NEEDED);

        image = new ImageView();
//        image.setPreserveRatio(true);

        setContent(image);
    }

    public void assignImageIndexTextField(final StringProperty imageIndexProperty) {
        this.imageIndexProperty = imageIndexProperty;
        imageIndexProperty.addListener(this);
    }

    @Override
    public void changed(ObservableValue<? extends String> ov, String t, String t1) {
        try {
            imageIndex = Integer.valueOf(t1);
            displayImage();
        } catch (NumberFormatException ex) {
            imageIndexProperty.setValue(t);
        }
    }

}