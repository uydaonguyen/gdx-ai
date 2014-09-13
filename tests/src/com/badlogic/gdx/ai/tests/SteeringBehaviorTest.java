/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.badlogic.gdx.ai.tests;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.ai.tests.steer.SteeringTest;
import com.badlogic.gdx.ai.tests.steer.box2d.tests.Box2dRaycastObstacleAvoidanceTest;
import com.badlogic.gdx.ai.tests.steer.box2d.tests.Box2dSeekTest;
import com.badlogic.gdx.ai.tests.steer.bullet.tests.BulletFollowPathTest;
import com.badlogic.gdx.ai.tests.steer.bullet.tests.BulletJumpTest;
import com.badlogic.gdx.ai.tests.steer.bullet.tests.BulletRaycastObstacleAvoidanceTest;
import com.badlogic.gdx.ai.tests.steer.bullet.tests.BulletSeekTest;
import com.badlogic.gdx.ai.tests.steer.scene2d.tests.ArriveTest;
import com.badlogic.gdx.ai.tests.steer.scene2d.tests.CollisionAvoidanceTest;
import com.badlogic.gdx.ai.tests.steer.scene2d.tests.FaceTest;
import com.badlogic.gdx.ai.tests.steer.scene2d.tests.FlockingTest;
import com.badlogic.gdx.ai.tests.steer.scene2d.tests.FollowFlowFieldTest;
import com.badlogic.gdx.ai.tests.steer.scene2d.tests.FollowPathTest;
import com.badlogic.gdx.ai.tests.steer.scene2d.tests.HideTest;
import com.badlogic.gdx.ai.tests.steer.scene2d.tests.InterposeTest;
import com.badlogic.gdx.ai.tests.steer.scene2d.tests.LookWhereYouAreGoingTest;
import com.badlogic.gdx.ai.tests.steer.scene2d.tests.PursueTest;
import com.badlogic.gdx.ai.tests.steer.scene2d.tests.RaycastObstacleAvoidanceTest;
import com.badlogic.gdx.ai.tests.steer.scene2d.tests.SeekTest;
import com.badlogic.gdx.ai.tests.steer.scene2d.tests.WanderTest;
import com.badlogic.gdx.ai.tests.utils.GdxAiTest;
import com.badlogic.gdx.ai.tests.utils.scene2d.CollapsableWindow;
import com.badlogic.gdx.ai.tests.utils.scene2d.TabbedPane;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.StringBuilder;

/** Test class for steering behaviors.
 * 
 * @author davebaol */
public class SteeringBehaviorTest extends GdxAiTest {

	private static final boolean DEBUG_STAGE = false;

	private static final String[] ENGINES = {"Scene2d", "Box2d", "Bullet"};

	public CollapsableWindow behaviorSelectionWindow;
	Label fpsLabel;
	StringBuilder fpsStringBuilder;

	// Keep it sorted!
	SteeringTest[][] behaviors = {
		{ // Scene2d
			new ArriveTest(this),
			new CollisionAvoidanceTest(this),
			new FaceTest(this),
			new FlockingTest(this),
			new FollowFlowFieldTest(this),
			new FollowPathTest(this, false),
			new FollowPathTest(this, true),
			new HideTest(this),
			new InterposeTest(this),
			new LookWhereYouAreGoingTest(this),
			new PursueTest(this),
			new RaycastObstacleAvoidanceTest(this),
			new SeekTest(this),
			new WanderTest(this)
		},
		{ // Box2d
			new Box2dRaycastObstacleAvoidanceTest(this),
			new Box2dSeekTest(this)
		},
		{ // Bullet
			new BulletFollowPathTest(this, false),
			new BulletFollowPathTest(this, true),
			new BulletJumpTest(this),
			new BulletRaycastObstacleAvoidanceTest(this),
			new BulletSeekTest(this),
		}
	};

	Table behaviorTable;
	SteeringTest currentBehavior;

	public Stage stage;
	public float stageWidth;
	public float stageHeight;
	public Skin skin;
	String behaviorNames[][];

	public TextureRegion greenFish;
	public TextureRegion cloud;
	public TextureRegion badlogicSmall;
	public TextureRegion target;

	@Override
	public void create () {
		Gdx.gl.glClearColor(.3f, .3f, .3f, 1);

		fpsStringBuilder = new StringBuilder();

		greenFish = new TextureRegion(new Texture("data/green_fish.png"));
		cloud = new TextureRegion(new Texture("data/particle-cloud.png"));
		badlogicSmall = new TextureRegion(new Texture("data/badlogicsmall.jpg"));
		target = new TextureRegion(new Texture("data/target.png"));

		skin = new Skin(Gdx.files.internal("data/uiskin.json"));

		stage = new Stage();
		stage.setDebugAll(DEBUG_STAGE);
		stageWidth = stage.getWidth();
		stageHeight = stage.getHeight();

		Gdx.input.setInputProcessor(new InputMultiplexer(stage));

		Stack stack = new Stack();
		stage.addActor(stack);
		stack.setSize(stageWidth, stageHeight);
		behaviorTable = new Table();
		stack.add(behaviorTable);

		// Create behavior selection window
		Array<List<String>> engineBehaviors = new Array<List<String>>(); 
		for (int k = 0; k < behaviors.length; k++) {
			engineBehaviors.add(createBehaviorList(k));
		}
		behaviorSelectionWindow = addBehaviorSelectionWindow ("Behaviors", ENGINES, engineBehaviors,  0, -1);

		// Set selected behavior
		changeBehavior(0, 0);

		fpsLabel = new Label("FPS: 999", skin);
		stage.addActor(fpsLabel);
	}

	@Override
	public void render () {
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		fpsStringBuilder.setLength(0);
		getStatus(fpsStringBuilder);
		fpsLabel.setText(fpsStringBuilder);

		if (currentBehavior != null) currentBehavior.render();

		stage.act();
		stage.draw();
	}

	@Override
	public void resize (int width, int height) {
		super.resize(width, height);
		stage.getViewport().update(width, height, true);
		stageWidth = width;
		stageHeight = height;
	}

	@Override
	public void dispose () {
		if (currentBehavior != null) currentBehavior.dispose();

		stage.dispose();
		skin.dispose();

		// Dispose textures
		greenFish.getTexture().dispose();
		cloud.getTexture().dispose();
		badlogicSmall.getTexture().dispose();
		target.getTexture().dispose();
	}

	protected void getStatus (final StringBuilder stringBuilder) {
		stringBuilder.append("FPS: ").append(Gdx.graphics.getFramesPerSecond());
	}

	private List<String> createBehaviorList(final int engineIndex) {
		// Create behavior names
		int numBehaviors = behaviors[engineIndex].length;
		String[] behaviorNames = new String[numBehaviors];
		for (int i = 0; i < numBehaviors; i++) {
			behaviorNames[i] = behaviors[engineIndex][i].behaviorName;
		}

		final List<String> behaviorsList = new List<String>(skin);
		behaviorsList.setItems(behaviorNames);
		behaviorsList.addListener(new ClickListener() {
			@Override
			public void clicked (InputEvent event, float x, float y) {
				if (!behaviorSelectionWindow.isCollapsed() && getTapCount() == 2) {
					changeBehavior(engineIndex, behaviorsList.getSelectedIndex());
					behaviorSelectionWindow.collapse();
				}
			}
		});
		return behaviorsList;
	}

	protected CollapsableWindow addBehaviorSelectionWindow (String title, String[] tabTitles, Array<List<String>> tabLists, float x, float y) {
		if (tabTitles.length != tabLists.size)
			throw new IllegalArgumentException("tabTitles and tabList must have the same size.");
		CollapsableWindow window = new CollapsableWindow(title, skin);
		window.row();
		TabbedPane tabbedPane = new TabbedPane(skin);
		for (int i = 0; i < tabLists.size; i++) {
			ScrollPane pane = new ScrollPane(tabLists.get(i), skin);
			pane.setFadeScrollBars(false);
			pane.setScrollX(0);
			pane.setScrollY(0);

			tabbedPane.addTab(tabTitles[i], pane);
		}
		window.add(tabbedPane);
		window.pack();
		window.pack();
		if (window.getHeight() > stage.getHeight()) {
			window.setHeight(stage.getHeight());
		}
		window.setX(x < 0 ? stage.getWidth() - (window.getWidth() - (x + 1)) : x);
		window.setY(y < 0 ? stage.getHeight() - (window.getHeight() - (y + 1)) : y);
		window.layout();
		window.collapse();
		stage.addActor(window);

		return window;
	}

	void changeBehavior (int engineIndex, int behaviorIndex) {
		// Remove the old behavior and its window
		behaviorTable.clear();
		if (currentBehavior != null) {
			if (currentBehavior.getDetailWindow() != null) currentBehavior.getDetailWindow().remove();
			currentBehavior.dispose();
		}

		// Add the new behavior and its window
		currentBehavior = behaviors[engineIndex][behaviorIndex];
		currentBehavior.create(behaviorTable);
		InputMultiplexer im = (InputMultiplexer)Gdx.input.getInputProcessor();
		if (im.size() > 1) im.removeProcessor(1);
		if (currentBehavior.getInputProcessor() != null) im.addProcessor(currentBehavior.getInputProcessor());
		if (currentBehavior.getDetailWindow() != null) stage.addActor(currentBehavior.getDetailWindow());
	}

}
