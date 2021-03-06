/**
 * JBoss, Home of Professional Open Source Copyright Red Hat, Inc., and
 * individual contributors by the
 *
 * @authors tag. See the copyright.txt in the distribution for a full listing of
 * individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by
 * applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS
 * OF ANY KIND, either express or implied. See the License for the specific
 * language governing permissions and limitations under the License.
 */
package org.jboss.aerogear.android.impl.pipeline;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.jboss.aerogear.MainFragmentActivity;
import org.jboss.aerogear.android.Pipeline;
import org.jboss.aerogear.android.Provider;
import org.jboss.aerogear.android.ReadFilter;
import org.jboss.aerogear.android.RecordId;
import org.jboss.aerogear.android.http.HeaderAndBody;
import org.jboss.aerogear.android.http.HttpProvider;
import org.jboss.aerogear.android.impl.helper.UnitTestUtils;
import org.jboss.aerogear.android.impl.http.HttpStubProvider;
import org.jboss.aerogear.android.impl.pipeline.loader.support.SupportReadLoader;
import org.jboss.aerogear.android.impl.pipeline.loader.support.SupportRemoveLoader;
import org.jboss.aerogear.android.impl.pipeline.loader.support.SupportSaveLoader;
import org.jboss.aerogear.android.pipeline.LoaderPipe;
import static org.jboss.aerogear.android.pipeline.LoaderPipe.*;
import org.jboss.aerogear.android.pipeline.Pipe;
import org.jboss.aerogear.android.pipeline.PipeHandler;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import android.annotation.TargetApi;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.test.ActivityInstrumentationTestCase2;

import com.google.gson.GsonBuilder;
import com.google.gson.InstanceCreator;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import org.jboss.aerogear.android.impl.util.VoidCallback;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
@SuppressWarnings({ "unchecked", "rawtypes" })
public class SupportLoaderAdapterTest extends
		ActivityInstrumentationTestCase2<MainFragmentActivity> {



	public SupportLoaderAdapterTest() {
		super(MainFragmentActivity.class);
	}

	private static final String SERIALIZED_POINTS = "{\"points\":[{\"x\":0,\"y\":0},{\"x\":1,\"y\":2},{\"x\":2,\"y\":4},{\"x\":3,\"y\":6},{\"x\":4,\"y\":8},{\"x\":5,\"y\":10},{\"x\":6,\"y\":12},{\"x\":7,\"y\":14},{\"x\":8,\"y\":16},{\"x\":9,\"y\":18}],\"id\":\"1\"}";
	private URL url;

	public void setUp() throws MalformedURLException {
		url = new URL("http://server.com/context/");
	}

	public void testSingleObjectRead() throws Exception {

		GsonBuilder builder = new GsonBuilder().registerTypeAdapter(
				Point.class, new PointTypeAdapter());
		HeaderAndBody response = new HeaderAndBody(
				SERIALIZED_POINTS.getBytes(), new HashMap<String, Object>());
		final HttpStubProvider provider = new HttpStubProvider(url, response);
		PipeConfig config = new PipeConfig(url,
				SupportLoaderAdapterTest.ListClassId.class);
		config.setGsonBuilder(builder);

		Pipeline pipeline = new Pipeline(url);

		Pipe<SupportLoaderAdapterTest.ListClassId> restPipe = pipeline.pipe(
				SupportLoaderAdapterTest.ListClassId.class, config);

		Object restRunner = UnitTestUtils.getPrivateField(restPipe,
				"restRunner");
		UnitTestUtils.setPrivateField(restRunner, "httpProviderFactory",
				new Provider<HttpProvider>() {
					@Override
					public HttpProvider get(Object... in) {
						return provider;
					}
				});

		LoaderPipe<SupportLoaderAdapterTest.ListClassId> adapter = pipeline
				.get(config.getName(), getActivity());
		adapter = Mockito.spy(adapter);
		adapter.read(new VoidCallback());

		ArgumentCaptor<Bundle> bundlerCaptor = ArgumentCaptor
				.forClass(Bundle.class);

		verify(
				(LoaderManager.LoaderCallbacks<SupportLoaderAdapterTest.ListClassId>) adapter)
				.onCreateLoader(Mockito.anyInt(), bundlerCaptor.capture());

		Bundle bundle = bundlerCaptor.getValue();
		assertNotNull(bundle.get(CALLBACK));
		assertTrue(bundle.get(CALLBACK) instanceof VoidCallback);
		assertNotNull(bundle.get(METHOD));
		assertTrue(((Enum) bundle.get(METHOD)).name().equals("READ"));
		assertNull(bundle.get(REMOVE_ID));

	}

	public void testSingleObjectDelete() throws Exception {

		GsonBuilder builder = new GsonBuilder().registerTypeAdapter(
				Point.class, new PointTypeAdapter());

		final HttpStubProvider provider = mock(HttpStubProvider.class);
		when(provider.getUrl()).thenReturn(url);

		PipeConfig config = new PipeConfig(url,
				SupportLoaderAdapterTest.ListClassId.class);
		config.setGsonBuilder(builder);

		Pipeline pipeline = new Pipeline(url);

		Pipe<SupportLoaderAdapterTest.ListClassId> restPipe = pipeline.pipe(
				SupportLoaderAdapterTest.ListClassId.class, config);

		Object restRunner = UnitTestUtils.getPrivateField(restPipe,
				"restRunner");
		UnitTestUtils.setPrivateField(restRunner, "httpProviderFactory",
				new Provider<HttpProvider>() {
					@Override
					public HttpProvider get(Object... in) {
						return provider;
					}
				});

		LoaderPipe<SupportLoaderAdapterTest.ListClassId> adapter = pipeline
				.get(config.getName(), getActivity());
		adapter = Mockito.spy(adapter);
		ArgumentCaptor<Bundle> bundlerCaptor = ArgumentCaptor
				.forClass(Bundle.class);

		adapter.remove("1", new VoidCallback());
		verify(
				(LoaderManager.LoaderCallbacks<SupportLoaderAdapterTest.ListClassId>) adapter)
				.onCreateLoader(Mockito.anyInt(), bundlerCaptor.capture());

		Bundle bundle = bundlerCaptor.getValue();
		assertNotNull(bundle.get(CALLBACK));
		assertTrue(bundle.get(CALLBACK) instanceof VoidCallback);
		assertNotNull(bundle.get(METHOD));
		assertTrue(((Enum) bundle.get(METHOD)).name().equals("REMOVE"));
		assertNotNull(bundle.get(REMOVE_ID));
		assertEquals("1", bundle.get(REMOVE_ID));

	}

	public void testMultipleCallsToLoadCallDeliver() {
		PipeHandler handler = mock(PipeHandler.class);
		final AtomicBoolean called = new AtomicBoolean(false);
		when(handler.onReadWithFilter((ReadFilter) any(), (Pipe) any()))
				.thenReturn(new ArrayList());
		SupportReadLoader loader = new SupportReadLoader(getActivity(), null,
				handler, null, null) {
			@Override
			public void deliverResult(Object data) {
				called.set(true);
				return;
			}

			@Override
			public void forceLoad() {
				throw new IllegalStateException("Should not be called");
			}

			@Override
			public void onStartLoading() {
				super.onStartLoading();
			}
		};
		loader.loadInBackground();
		UnitTestUtils.callMethod(loader, "onStartLoading");

		assertTrue(called.get());

	}

	public void testMultipleCallsToSaveCallDeliver() {
		PipeHandler handler = mock(PipeHandler.class);
		final AtomicBoolean called = new AtomicBoolean(false);
		when(handler.onSave(any())).thenReturn(new ArrayList());
		SupportSaveLoader loader = new SupportSaveLoader(getActivity(), null,
				handler, null) {
			@Override
			public void deliverResult(Object data) {
				called.set(true);
				return;
			}

			@Override
			public void forceLoad() {
				throw new IllegalStateException("Should not be called");
			}

			@Override
			public void onStartLoading() {
				super.onStartLoading();
			}
		};
		loader.loadInBackground();
		UnitTestUtils.callMethod(loader, "onStartLoading");

		assertTrue(called.get());

	}

	public void testMultipleCallsToRemoveCallDeliver() {
		PipeHandler handler = mock(PipeHandler.class);
		final AtomicBoolean called = new AtomicBoolean(false);
		when(handler.onReadWithFilter((ReadFilter) any(), (Pipe) any()))
				.thenReturn(new ArrayList());
		SupportRemoveLoader loader = new SupportRemoveLoader(getActivity(),
				null, handler, null) {
			@Override
			public void deliverResult(Object data) {
				called.set(true);
				return;
			}

			@Override
			public void forceLoad() {
				throw new IllegalStateException("Should not be called");
			}

			@Override
			public void onStartLoading() {
				super.onStartLoading();
			}
		};
		loader.loadInBackground();
		UnitTestUtils.callMethod(loader, "onStartLoading");

		assertTrue(called.get());

	}

	private static class PointTypeAdapter implements InstanceCreator,
			JsonSerializer, JsonDeserializer {

		@Override
		public Object createInstance(Type type) {
			return new Point();
		}

		@Override
		public JsonElement serialize(Object src, Type typeOfSrc,
				JsonSerializationContext context) {
			JsonObject object = new JsonObject();
			object.addProperty("x", ((Point) src).x);
			object.addProperty("y", ((Point) src).y);
			return object;
		}

		@Override
		public Object deserialize(JsonElement json, Type typeOfT,
				JsonDeserializationContext context) throws JsonParseException {
			return new Point(json.getAsJsonObject().getAsJsonPrimitive("x")
					.getAsInt(), json.getAsJsonObject().getAsJsonPrimitive("y")
					.getAsInt());
		}
	}

	public final static class ListClassId {

		List<Point> points = new ArrayList<Point>(10);
		@RecordId
		String id = "1";

		public ListClassId(boolean build) {
			if (build) {
				for (int i = 0; i < 10; i++) {
					points.add(new Point(i, i * 2));
				}
			}
		}

		public ListClassId() {
		}

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

		@Override
		public boolean equals(Object obj) {
			try {
				return points.equals(((ListClassId) obj).points);
			} catch (Throwable ignore) {
				return false;
			}
		}
	}

	
}
