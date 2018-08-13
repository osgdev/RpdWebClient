package uk.gov.dvla.osg.rpd.web.json;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.*;

/**
 * A factory for creating AlwaysListTypeAdapter objects.
 *
 * @param <E> the element type
 */
public final class AlwaysListTypeAdapterFactory<E> implements TypeAdapterFactory {

	/**
	 * Instantiates a new always list type adapter factory.
	 */
	private AlwaysListTypeAdapterFactory() {}

	@Override
	public <T> TypeAdapter<T> create(final Gson gson, final TypeToken<T> typeToken) {
		// If it's not a List -- just delegate the job to Gson and let it pick the best type adapter itself
		if (!List.class.isAssignableFrom(typeToken.getRawType())) {
			return null;
		}
		// Resolving the list parameter type
		final Type elementType = resolveTypeArgument(typeToken.getType());
		final TypeAdapter<E> elementTypeAdapter = (TypeAdapter<E>) gson.getAdapter(TypeToken.get(elementType));
		// Note that the always-list type adapter is made null-safe, so we don't have to check nulls ourselves
		final TypeAdapter<T> alwaysListTypeAdapter = (TypeAdapter<T>) new AlwaysListTypeAdapter<>(elementTypeAdapter)
				.nullSafe();
		return alwaysListTypeAdapter;
	}

	/**
	 * Resolve type argument.
	 *
	 * @param type the type
	 * @return the type
	 */
	private static Type resolveTypeArgument(final Type type) {
		// The given type is not parameterized?
		if (!(type instanceof ParameterizedType)) {
			// No, raw
			return Object.class;
		}
		final ParameterizedType parameterizedType = (ParameterizedType) type;
		return parameterizedType.getActualTypeArguments()[0];
	}

	/**
	 * The Class AlwaysListTypeAdapter.
	 * @param <E> the element type
	 */
	private static final class AlwaysListTypeAdapter<E> extends TypeAdapter<List<E>> {

		private final TypeAdapter<E> elementTypeAdapter;

		/**
		 * Instantiates a new always list type adapter.
		 * @param elementTypeAdapter the element type adapter
		 */
		private AlwaysListTypeAdapter(final TypeAdapter<E> elementTypeAdapter) {
			this.elementTypeAdapter = elementTypeAdapter;
		}

		@Override
		public void write(final JsonWriter out, final List<E> list) {
			throw new UnsupportedOperationException();
		}

		@Override
		public List<E> read(final JsonReader in) throws IOException {
			// This is where we detect the list "type"
			final List<E> list = new ArrayList<>();
			final JsonToken token = in.peek();
			switch (token) {
			case BEGIN_ARRAY:
				// If it's a regular list, just consume [, <all elements>, and ]
				in.beginArray();
				while (in.hasNext()) {
					list.add(elementTypeAdapter.read(in));
				}
				in.endArray();
				break;
			case BEGIN_OBJECT:
			case STRING:
			case NUMBER:
			case BOOLEAN:
				// An object or a primitive? Just add the current value to the result list
				list.add(elementTypeAdapter.read(in));
				break;
			case NULL:
				throw new AssertionError("Must never happen: check if the type adapter configured with .nullSafe()");
			case NAME:
			case END_ARRAY:
			case END_OBJECT:
			case END_DOCUMENT:
				throw new MalformedJsonException("Unexpected token: " + token);
			default:
				throw new AssertionError("Must never happen: " + token);
			}
			return list;
		}

	}
}