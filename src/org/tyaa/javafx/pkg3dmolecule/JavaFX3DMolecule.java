/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.tyaa.javafx.pkg3dmolecule;

import javafx.animation.RotateTransition;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.scene.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.ToolBar;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 *
 * @author yurii
 */
public class JavaFX3DMolecule extends Application {
    
    private Group root;
  private SubScene subScene;
  final ColorAdjust colorEffect = new ColorAdjust();
  final Xform axisGroup = new Xform();
  final Xform moleculeGroup = new Xform();
  final Xform world = new Xform();
  final PerspectiveCamera camera = new PerspectiveCamera(true);
  final Xform cameraXform = new Xform();
  final Xform cameraXform2 = new Xform();
  final Xform cameraXform3 = new Xform();
  private static final double CAMERA_INITIAL_DISTANCE = -450;
  private static final double CAMERA_INITIAL_X_ANGLE = 70.0;
  private static final double CAMERA_INITIAL_Y_ANGLE = 320.0;
  private static final double CAMERA_NEAR_CLIP = 0.1;
  private static final double CAMERA_FAR_CLIP = 10000.0;
  private static final double AXIS_LENGTH = 250.0;
  private static final double HYDROGEN_ANGLE = 104.5;
  private static final double CONTROL_MULTIPLIER = 0.1;
  private static final double SHIFT_MULTIPLIER = 10.0;
  private static final double MOUSE_SPEED = 0.1;
  private static final double ROTATION_SPEED = 2.0;
  private static final double TRACK_SPEED = 0.3;

  double mousePosX;
  double mousePosY;
  double mouseOldX;
  double mouseOldY;
  double mouseDeltaX;
  double mouseDeltaY;

  /**
   * Prepare the camera
   *
   */
  private void buildCamera()
  {
    System.out.println("buildCamera()");
    root.getChildren().add(cameraXform);
    cameraXform.getChildren().add(cameraXform2);
    cameraXform2.getChildren().add(cameraXform3);
    cameraXform3.getChildren().add(camera);
    cameraXform3.setRotateZ(180.0);

    camera.setNearClip(CAMERA_NEAR_CLIP);
    camera.setFarClip(CAMERA_FAR_CLIP);
    camera.setTranslateZ(CAMERA_INITIAL_DISTANCE);
    cameraXform.ry.setAngle(CAMERA_INITIAL_Y_ANGLE);
    cameraXform.rx.setAngle(CAMERA_INITIAL_X_ANGLE);
  }

  /**
   * Handle for when the user uses the mouse to rotate the molecule being displayed.
   *
   * @param scene - the scene where the action applies.
   * @param root - root node.
   */
  private void handleMouse(Scene scene, final Node root)
  {
    scene.setOnMousePressed(new EventHandler<MouseEvent>()
    {
      @Override
      public void handle(MouseEvent me)
      {
        mousePosX = me.getSceneX();
        mousePosY = me.getSceneY();
        mouseOldX = me.getSceneX();
        mouseOldY = me.getSceneY();
      }
    });
    scene.setOnMouseDragged(new EventHandler<MouseEvent>()
    {
      @Override
      public void handle(MouseEvent me)
      {
        mouseOldX = mousePosX;
        mouseOldY = mousePosY;
        mousePosX = me.getSceneX();
        mousePosY = me.getSceneY();
        mouseDeltaX = (mousePosX - mouseOldX);
        mouseDeltaY = (mousePosY - mouseOldY);

        double modifier = 1.0;

        if (me.isControlDown())
        {
          modifier = CONTROL_MULTIPLIER;
        }
        if (me.isShiftDown())
        {
          modifier = SHIFT_MULTIPLIER;
        }
        if (me.isPrimaryButtonDown())
        {
          cameraXform.ry.setAngle(cameraXform.ry.getAngle() - mouseDeltaX * MOUSE_SPEED * modifier * ROTATION_SPEED);
          cameraXform.rx.setAngle(cameraXform.rx.getAngle() + mouseDeltaY * MOUSE_SPEED * modifier * ROTATION_SPEED);
        } else if (me.isSecondaryButtonDown())
        {
          double z = camera.getTranslateZ();
          double newZ = z + mouseDeltaX * MOUSE_SPEED * modifier;
          camera.setTranslateZ(newZ);
        } else if (me.isMiddleButtonDown())
        {
          cameraXform2.t.setX(cameraXform2.t.getX() + mouseDeltaX * MOUSE_SPEED * modifier * TRACK_SPEED);
          cameraXform2.t.setY(cameraXform2.t.getY() + mouseDeltaY * MOUSE_SPEED * modifier * TRACK_SPEED);
        }
      }
    });
  }

  /**
   * Play a quick rotation animation on the currently selected molecule.
   *
   */
  private void rotateAnimation()
  {
    RotateTransition rt = new RotateTransition(Duration.millis(3000), moleculeGroup);
    rt.setByAngle(360);
    rt.setCycleCount(4);
    rt.setAutoReverse(false);

    rt.play();
  }

  /**
   * Prepare the water molecule for viewing in 3D.
   */
  private void buildWaterMolecule()
  {
    moleculeGroup.getChildren().clear();
    world.getChildren().clear();

    final PhongMaterial redMaterial = new PhongMaterial();
    redMaterial.setDiffuseColor(Color.DARKRED);
    redMaterial.setSpecularColor(Color.RED);

    final PhongMaterial whiteMaterial = new PhongMaterial();
    whiteMaterial.setDiffuseColor(Color.WHITE);
    whiteMaterial.setSpecularColor(Color.LIGHTBLUE);

    final PhongMaterial greyMaterial = new PhongMaterial();
    greyMaterial.setDiffuseColor(Color.DARKGREY);
    greyMaterial.setSpecularColor(Color.GREY);

    // Molecule Hierarchy
    // [*] moleculeXform
    //     [*] oxygenXform
    //         [*] oxygenSphere
    //     [*] hydrogen1SideXform
    //         [*] hydrogen1Xform
    //             [*] hydrogen1Sphere
    //         [*] bond1Cylinder
    //     [*] hydrogen2SideXform
    //         [*] hydrogen2Xform
    //             [*] hydrogen2Sphere
    //         [*] bond2Cylinder

    Xform moleculeXform = new Xform();
    Xform oxygenXform = new Xform();
    Xform hydrogen1SideXform = new Xform();
    Xform hydrogen1Xform = new Xform();
    Xform hydrogen2SideXform = new Xform();
    Xform hydrogen2Xform = new Xform();

    Sphere oxygenSphere = new Sphere(40.0);
    oxygenSphere.setMaterial(redMaterial);

    Sphere hydrogen1Sphere = new Sphere(30.0);
    hydrogen1Sphere.setMaterial(whiteMaterial);
    hydrogen1Sphere.setTranslateX(0.0);

    Sphere hydrogen2Sphere = new Sphere(30.0);
    hydrogen2Sphere.setMaterial(whiteMaterial);
    hydrogen2Sphere.setTranslateZ(0.0);

    Cylinder bond1Cylinder = new Cylinder(5, 100);
    bond1Cylinder.setMaterial(greyMaterial);
    bond1Cylinder.setTranslateX(50.0);
    bond1Cylinder.setRotationAxis(Rotate.Z_AXIS);
    bond1Cylinder.setRotate(90.0);

    Cylinder bond2Cylinder = new Cylinder(5, 100);
    bond2Cylinder.setMaterial(greyMaterial);
    bond2Cylinder.setTranslateX(50.0);
    bond2Cylinder.setRotationAxis(Rotate.Z_AXIS);
    bond2Cylinder.setRotate(90.0);

    moleculeXform.getChildren().add(oxygenXform);
    moleculeXform.getChildren().add(hydrogen1SideXform);
    moleculeXform.getChildren().add(hydrogen2SideXform);
    oxygenXform.getChildren().add(oxygenSphere);
    hydrogen1SideXform.getChildren().add(hydrogen1Xform);
    hydrogen2SideXform.getChildren().add(hydrogen2Xform);
    hydrogen1Xform.getChildren().add(hydrogen1Sphere);
    hydrogen2Xform.getChildren().add(hydrogen2Sphere);
    hydrogen1SideXform.getChildren().add(bond1Cylinder);
    hydrogen2SideXform.getChildren().add(bond2Cylinder);

    hydrogen1Xform.setTx(100.0);
    hydrogen2Xform.setTx(100.0);
    hydrogen2SideXform.setRotateY(HYDROGEN_ANGLE);

    moleculeGroup.getChildren().add(moleculeXform);

    world.getChildren().addAll(moleculeGroup);

    rotateAnimation();
  }

  /**
   * Prepare the methane molecule for viewing in 3D.
   */
  private void buildMethaneMolecule()
  {
//    make sure the root is clear of all children nodes
    moleculeGroup.getChildren().clear();
    world.getChildren().clear();

    final PhongMaterial redMaterial = new PhongMaterial();
    redMaterial.setDiffuseColor(Color.DARKRED);
    redMaterial.setSpecularColor(Color.RED);

    final PhongMaterial whiteMaterial = new PhongMaterial();
    whiteMaterial.setDiffuseColor(Color.WHITE);
    whiteMaterial.setSpecularColor(Color.LIGHTBLUE);

    final PhongMaterial greyMaterial = new PhongMaterial();
    greyMaterial.setDiffuseColor(Color.DARKGREY);
    greyMaterial.setSpecularColor(Color.GREY);

    Xform moleculeXform = new Xform();
    Xform carbon1Xform = new Xform();
    Xform hydrogen1SideXform = new Xform();
    Xform hydrogen1Xform = new Xform();
    Xform hydrogen2SideXform = new Xform();
    Xform hydrogen2Xform = new Xform();
    Xform hydrogen3SideXform = new Xform();
    Xform hydrogen3Xform = new Xform();
    Xform hydrogen4Xform = new Xform();
    Xform hydrogen4SideXform = new Xform();

//    create sphere representing carbon atom
    Sphere carbon1Sphere = new Sphere(40.0);
    carbon1Sphere.setMaterial(redMaterial);

//    create spheres representing the 4 hydrogens
    Sphere hydrogen1Sphere = new Sphere(30.0);
    hydrogen1Sphere.setMaterial(whiteMaterial);
    hydrogen1Sphere.setTranslateX(0.0);

    Sphere hydrogen2Sphere = new Sphere(30.0);
    hydrogen2Sphere.setMaterial(whiteMaterial);
    hydrogen2Sphere.setTranslateZ(0.0);

    Sphere hydrogen3Sphere = new Sphere(30.0);
    hydrogen3Sphere.setMaterial(whiteMaterial);
    hydrogen3Sphere.setTranslateY(0.0);

    Sphere hydrogen4Sphere = new Sphere(30.0);
    hydrogen2Sphere.setMaterial(whiteMaterial);


//    cylinders for 4 bonds
    Cylinder bond1Cylinder = new Cylinder(5, 100);
    bond1Cylinder.setMaterial(greyMaterial);
    bond1Cylinder.setTranslateX(50.0);
    bond1Cylinder.setRotationAxis(Rotate.Z_AXIS);
    bond1Cylinder.setRotate(90.0);

    Cylinder bond2Cylinder = new Cylinder(5, 100);
    bond2Cylinder.setMaterial(greyMaterial);
    bond2Cylinder.setTranslateX(50.0);
    bond2Cylinder.setRotationAxis(Rotate.Z_AXIS);
    bond2Cylinder.setRotate(90.0);

    Cylinder bond3Cylinder = new Cylinder(5, 100);
    bond3Cylinder.setMaterial(greyMaterial);
    bond3Cylinder.setTranslateX(50.0);
    bond3Cylinder.setRotationAxis(Rotate.Z_AXIS);
    bond3Cylinder.setRotate(90.0);

    Cylinder bond4Cylinder = new Cylinder(5, 100);
    bond4Cylinder.setMaterial(greyMaterial);
    bond4Cylinder.setTranslateY(35.0);
    bond4Cylinder.setTranslateZ(35.0);
    bond4Cylinder.setRotationAxis(Rotate.X_AXIS);
    bond4Cylinder.setRotate(45.0);

    moleculeXform.getChildren().add(carbon1Xform);
    moleculeXform.getChildren().add(hydrogen4SideXform);
    moleculeXform.getChildren().add(hydrogen1SideXform);
    moleculeXform.getChildren().add(hydrogen2SideXform);
    moleculeXform.getChildren().add(hydrogen3SideXform);
    carbon1Xform.getChildren().add(carbon1Sphere);
    hydrogen4SideXform.getChildren().add(hydrogen4Xform);
    hydrogen4Xform.getChildren().add(hydrogen4Sphere);
    hydrogen1SideXform.getChildren().add(hydrogen1Xform);
    hydrogen2SideXform.getChildren().add(hydrogen2Xform);
    hydrogen3SideXform.getChildren().add(hydrogen3Xform);
    hydrogen1Xform.getChildren().add(hydrogen1Sphere);
    hydrogen2Xform.getChildren().add(hydrogen2Sphere);
    hydrogen3Xform.getChildren().add(hydrogen3Sphere);

    hydrogen1SideXform.getChildren().add(bond1Cylinder);
    hydrogen2SideXform.getChildren().add(bond2Cylinder);
    hydrogen3SideXform.getChildren().add(bond3Cylinder);
    hydrogen4SideXform.getChildren().add(bond4Cylinder);


    hydrogen1Xform.setTx(100.0);
    hydrogen2Xform.setTx(100.0);
    hydrogen3Xform.setTx(100.0);
    hydrogen4Xform.setTy(70.0);
    hydrogen4Xform.setTz(70.0);
    hydrogen2SideXform.setRotateY(HYDROGEN_ANGLE);
    hydrogen3SideXform.setRotateZ(HYDROGEN_ANGLE);
    hydrogen4SideXform.setRotateX(HYDROGEN_ANGLE);

    moleculeGroup.getChildren().add(moleculeXform);

    world.getChildren().addAll(moleculeGroup);

    rotateAnimation();
  }

  @Override
  /**
   * Overridden start method, takes a Stage element
   *
   * Sets up the JavaFX scene for drawing and calls functions
   * for building the camera, axes, and molecule.
   *
   * @param primaryStage - the main stage.
   */
  public void start(Stage primaryStage)
  {
    root = new Group();

    buildCamera();
    buildMethaneMolecule();

    subScene = new SubScene(root, 1024, 768, true, SceneAntialiasing.BALANCED);
    subScene.setFill(Color.GREY);
    subScene.setCamera(camera);
    subScene.setEffect(colorEffect);

    root.getChildren().add(world);

    // 2D
    BorderPane pane = new BorderPane();
    pane.setCenter(subScene);

    Button methaneButton = new Button("Methane (CH4)");
    methaneButton.setOnAction(e->
    {
      buildMethaneMolecule();
    });

    Button waterButton = new Button("Water (H2O)");
    waterButton.setOnAction(e->
    {
      buildWaterMolecule();
    });

    Label brightnessLabel = new Label("Adjust Brightness");
    Slider brightnessSlider = new Slider();
    brightnessSlider.setMin(0);
    brightnessSlider.setMax(1.0);

    brightnessSlider.valueProperty().addListener(new ChangeListener<Number>() {
      public void changed(ObservableValue<? extends Number> ov,
                          Number old_val, Number new_val) {
        colorEffect.setBrightness(new_val.doubleValue());
      }
    });

    Label contrastLabel = new Label("Adjust Contrast");
    Slider contrastSlider = new Slider();
    contrastSlider.setMin(0);
    contrastSlider.setMax(1.0);

    contrastSlider.valueProperty().addListener(new ChangeListener<Number>() {
      public void changed(ObservableValue<? extends Number> ov,
                          Number old_val, Number new_val) {
        colorEffect.setContrast(new_val.doubleValue());
      }
    });

    ToolBar toolBar = new ToolBar(methaneButton, waterButton, brightnessLabel, brightnessSlider,
            contrastLabel, contrastSlider);
    toolBar.setOrientation(Orientation.VERTICAL);
    pane.setRight(toolBar);
    pane.setPrefSize(300,300);

    Scene scene = new Scene(pane);
    handleMouse(scene, world);

    primaryStage.setScene(scene);
    primaryStage.setTitle("JavaFX 3D Molecule Example");
    primaryStage.show();
  }

  /**
   * The main() method is ignored in correctly deployed JavaFX application.
   * main() serves only as fallback in case the application can not be
   * launched through deployment artifacts, e.g., in IDEs with limited FX
   * support. NetBeans ignores main().
   *
   * @param args the command line arguments
   */
  public static void main(String[] args)
  {
    launch(args);
  }
    
}
