/*
 * Odoo, Open Source Management Solution
 * Copyright (C) 2012-today Odoo SA (<http:www.odoo.com>)
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http:www.gnu.org/licenses/>
 * 
 */
package odoo.controls;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import com.odoo.crm.R;
import com.odoo.orm.ODataRow;
import com.odoo.support.listview.OListAdapter;

/**
 * The Class OList.
 */
@SuppressLint("ClickableViewAccessibility")
public class OList extends ScrollView implements View.OnClickListener,
		View.OnLongClickListener, View.OnTouchListener, View.OnDragListener {

	/** The Constant KEY_CUSTOM_LAYOUT. */
	public static final String KEY_CUSTOM_LAYOUT = "custome_layout";

	/** The Constant KEY_SHOW_DIVIDER. */
	public static final String KEY_SHOW_DIVIDER = "showDivider";

	/** The context. */
	Context mContext = null;

	/** The typed array. */
	TypedArray mTypedArray = null;

	/** The list adapter. */
	OListAdapter mListAdapter = null;

	/** The records. */
	List<Object> mRecords = new ArrayList<Object>();

	/** The attr. */
	OControlAttributes mAttr = new OControlAttributes();

	/** The custom layout. */
	Integer mCustomLayout = 0;

	/** The inner layout. */
	LinearLayout mInnerLayout = null;

	/** The layout params. */
	LayoutParams mLayoutParams = null;

	/** The on row click listener. */
	OnRowClickListener mOnRowClickListener = null;

	/** The row draggable. */
	private Boolean mRowDraggable = false;

	/** The drag mode. */
	private boolean mDragMode = false;

	/** The drag drop listener. */
	private OListDragDropListener mDragDropListener = null;

	/** The shadow builder. */
	private View.DragShadowBuilder mShadowBuilder = null;

	/** The drop layouts. */
	private List<Integer> mDropLayouts = new ArrayList<Integer>();

	/** The dropped. */
	private Boolean mDropped = false;

	/** The row droppable. */
	private Boolean mRowDroppable = false;

	/** The draggable view. */
	private View mDraggableView = null;

	/** The drag started. */
	private Boolean mDragStarted = false;

	/** The drag ended. */
	private Boolean mDragEnded = false;

	/** The m view click listener. */
	private List<ViewClickListeners> mViewClickListener = new ArrayList<ViewClickListeners>();

	/** The m before list row create listener. */
	private BeforeListRowCreateListener mBeforeListRowCreateListener = null;

	/**
	 * Instantiates a new list control.
	 * 
	 * @param context
	 *            the context
	 */
	public OList(Context context) {
		super(context);
		init(context, null, 0);
	}

	/**
	 * Instantiates a new list control.
	 * 
	 * @param context
	 *            the context
	 * @param attrs
	 *            the attrs
	 */
	public OList(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs, 0);
	}

	/**
	 * Instantiates a new list control.
	 * 
	 * @param context
	 *            the context
	 * @param attrs
	 *            the attrs
	 * @param defStyle
	 *            the def style
	 */
	public OList(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context, attrs, defStyle);
	}

	/**
	 * Inits the list control.
	 * 
	 * @param context
	 *            the context
	 * @param attrs
	 *            the attrs
	 * @param defStyle
	 *            the def style
	 */
	private void init(Context context, AttributeSet attrs, int defStyle) {
		mContext = context;
		if (attrs != null) {
			mTypedArray = mContext.obtainStyledAttributes(attrs,
					R.styleable.OList);
			mAttr.put(KEY_CUSTOM_LAYOUT, mTypedArray.getResourceId(
					R.styleable.OList_custom_layout, 0));
			mAttr.put(KEY_SHOW_DIVIDER,
					mTypedArray.getBoolean(R.styleable.OList_showDivider, true));
			mCustomLayout = mAttr.getResource(KEY_CUSTOM_LAYOUT, 0);
			mTypedArray.recycle();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.View#onFinishInflate()
	 */
	protected void onFinishInflate() {
		super.onFinishInflate();
		removeAllViews();
		createListInnerControl();
	}

	/**
	 * Creates the list inner control.
	 */
	private void createListInnerControl() {
		mInnerLayout = parentView();
	}

	/**
	 * Inits the list control.
	 * 
	 * @param records
	 *            the records
	 */
	public void initListControl(List<ODataRow> records) {
		mRecords.clear();
		mRecords.addAll(records);
		createAdapter();
	}

	/**
	 * Creates the adapter.
	 */
	private void createAdapter() {
		mListAdapter = new OListAdapter(mContext, mCustomLayout, mRecords) {
			@Override
			public View getView(final int position, View convertView,
					ViewGroup parent) {
				View mView = (View) convertView;
				LayoutInflater inflater = LayoutInflater.from(mContext);
				if (mView == null) {
					mView = inflater.inflate(getResource(), parent, false);
				}
				final ODataRow record = (ODataRow) mRecords.get(position);
				final OForm form = (OForm) mView;
				form.initForm(record);
				for (final ViewClickListeners listener : mViewClickListener) {
					for (final String key : listener.getKeys()) {
						form.setOnViewClickListener(listener.getViewId(key),
								new OForm.OnViewClickListener() {

									@Override
									public void onFormViewClick(View view,
											ODataRow row) {
										listener.getListener(key)
												.onRowViewClick(form, view,
														position, record);
									}
								});
					}
				}
				if (mBeforeListRowCreateListener != null) {
					mBeforeListRowCreateListener.beforeListRowCreate(position,
							record, mView);
				}
				return mView;
			}
		};
		addRecordViews();
	}

	/**
	 * Adds the record views.
	 */
	private void addRecordViews() {
		removeAllViews();
		mInnerLayout.removeAllViews();
		for (int i = 0; i < mListAdapter.getCount(); i++) {
			OForm view = (OForm) mListAdapter.getView(i, null, null);
			view.setTag(i);
			if (mOnRowClickListener != null) {
				view.setOnClickListener(this);
			}
			if (mRowDraggable) {
				view.setOnLongClickListener(this);
				view.setOnTouchListener(this);
				view.setOnDragListener(this);
			}
			if (mRowDroppable) {
				view.setOnDragListener(this);
			}
			mInnerLayout.addView(view);
			if (mAttr.getBoolean(KEY_SHOW_DIVIDER, true))
				mInnerLayout.addView(divider());
		}
		addView(mInnerLayout);
	}

	/**
	 * Parent view.
	 * 
	 * @return the linear layout
	 */
	private LinearLayout parentView() {
		LinearLayout mLayout = new LinearLayout(mContext);
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		mLayout.setLayoutParams(params);
		mLayout.setOrientation(LinearLayout.VERTICAL);
		return mLayout;
	}

	/**
	 * Divider.
	 * 
	 * @return the view
	 */
	private View divider() {
		View v = new View(mContext);
		v.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, 1));
		v.setBackgroundColor(mContext.getResources().getColor(
				R.color.list_divider));
		return v;
	}

	/**
	 * Sets the custom view.
	 * 
	 * @param view_resource
	 *            the new custom view
	 */
	public void setCustomView(int view_resource) {
		mAttr.put(KEY_CUSTOM_LAYOUT, view_resource);
		mCustomLayout = view_resource;
	}

	/**
	 * Sets the on row click listener.
	 * 
	 * @param listener
	 *            the new on row click listener
	 */
	public void setOnRowClickListener(OnRowClickListener listener) {
		mOnRowClickListener = listener;
	}

	/**
	 * The listener interface for receiving onRowClick events. The class that is
	 * interested in processing a onRowClick event implements this interface,
	 * and the object created with that class is registered with a component
	 * using the component's <code>addOnRowClickListener<code> method. When
	 * the onRowClick event occurs, that object's appropriate
	 * method is invoked.
	 * 
	 * @see OnRowClickEvent
	 */
	public interface OnRowClickListener {

		/**
		 * On row item click.
		 * 
		 * @param position
		 *            the position
		 * @param view
		 *            the view
		 * @param row
		 *            the row
		 */
		public void onRowItemClick(int position, View view, ODataRow row);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.View.OnClickListener#onClick(android.view.View)
	 */
	@Override
	public void onClick(View v) {
		if (!mDragMode) {
			int pos = (Integer) v.getTag();
			mOnRowClickListener.onRowItemClick(pos, v,
					(ODataRow) mRecords.get(pos));
		}
	}

	/**
	 * Sets the drag drop listener.
	 * 
	 * @param listener
	 *            the new drag drop listener
	 */
	public void setDragDropListener(OListDragDropListener listener) {
		mDragDropListener = listener;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.View.OnLongClickListener#onLongClick(android.view.View)
	 */
	@Override
	public boolean onLongClick(View v) {
		mDragMode = true;
		mDragStarted = false;
		mDragEnded = false;
		return true;
	}

	/**
	 * The Class DragShadowBuilder.
	 */
	private static class DragShadowBuilder extends View.DragShadowBuilder {

		/** The shadow. */
		private static Drawable shadow;

		/** The height. */
		int width, height;

		/**
		 * Instantiates a new drag shadow builder.
		 * 
		 * @param v
		 *            the v
		 */
		public DragShadowBuilder(View v) {
			super(v);
			shadow = new ColorDrawable(Color.LTGRAY);
			width = getView().getWidth() / 2;
			height = getView().getHeight() / 2;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * android.view.View.DragShadowBuilder#onProvideShadowMetrics(android
		 * .graphics.Point, android.graphics.Point)
		 */
		@Override
		public void onProvideShadowMetrics(Point size, Point touch) {
			shadow.setBounds(0, 0, width, height);
			size.set(width, height);
			touch.set(width / 2, height / 2);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * android.view.View.DragShadowBuilder#onDrawShadow(android.graphics
		 * .Canvas)
		 */
		@Override
		public void onDrawShadow(Canvas canvas) {
			shadow.draw(canvas);
		}

	}

	/**
	 * Sets the row draggable.
	 * 
	 * @param draggable
	 *            the new row draggable
	 */
	public void setRowDraggable(boolean draggable) {
		mRowDraggable = draggable;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.View.OnTouchListener#onTouch(android.view.View,
	 * android.view.MotionEvent)
	 */
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if (mDragMode && event.getAction() == MotionEvent.ACTION_MOVE) {
			mShadowBuilder = new DragShadowBuilder(v);
			v.startDrag(null, mShadowBuilder, v, 0);
			v.setVisibility(View.INVISIBLE);
			return true;
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.View.OnDragListener#onDrag(android.view.View,
	 * android.view.DragEvent)
	 */
	@Override
	public boolean onDrag(final View v, DragEvent event) {
		int action = event.getAction();
		final View view = (View) event.getLocalState();
		ViewGroup parent = (ViewGroup) view.getParent();
		ViewGroup newParent = (ViewGroup) v;
		final int position = (Integer) view.getTag();
		switch (action) {
		case DragEvent.ACTION_DRAG_STARTED:
			if (mRowDraggable && mDragDropListener != null)
				onDragStart(view, position, mRecords.get(position));
			break;
		case DragEvent.ACTION_DRAG_ENTERED:
			if (mRowDroppable || isDroppable(v))
				v.setBackgroundColor(Color.GRAY);
			break;
		case DragEvent.ACTION_DRAG_EXITED:
			if (mRowDroppable || isDroppable(v))
				v.setBackgroundColor(Color.WHITE);
			break;
		case DragEvent.ACTION_DROP:
			if (mRowDroppable || isDroppable(v)) {
				parent.removeView(view);
				view.setVisibility(View.VISIBLE);
				view.setOnTouchListener(null);
				newParent.setBackgroundColor(Color.WHITE);
				if (getDraggableView() instanceof OList) {
					int drop_position = (Integer) newParent.getTag();
					OList draggableView = (OList) getDraggableView();
					draggableView.setDroppedObjectData(view, position,
							mRecords.get(drop_position));
				}
				if (getDraggableView() == null && isDroppable(v)) {
					// Adding view to layout
					newParent.addView(view);
				}
			}
			mDragMode = false;
			mDropped = true;
			break;
		case DragEvent.ACTION_DRAG_ENDED:
			if (!mDropped) {
				view.post(new Runnable() {
					@Override
					public void run() {
						view.setVisibility(View.VISIBLE);
					}
				});
			}
			if (mDragDropListener != null) {
				onDragEnnd(view, position, mRecords.get(position));
			}
			mDragMode = false;
			mDropped = false;
			break;
		}
		return true;
	}

	/**
	 * Sets the dropped object data.
	 * 
	 * @param view
	 *            the view
	 * @param position
	 *            the position
	 * @param object
	 *            the object
	 */
	public void setDroppedObjectData(View view, int position, Object object) {
		if (mDragDropListener != null) {
			mDragDropListener.onItemDrop(view, mRecords.get(position), object);
		}
	}

	/**
	 * On drag start.
	 * 
	 * @param view
	 *            the view
	 * @param position
	 *            the position
	 * @param object
	 *            the object
	 */
	private void onDragStart(View view, int position, Object object) {
		if (!mDragStarted) {
			mDragDropListener.onItemDragStart(view, position, object);
			mDragStarted = true;
		}
	}

	/**
	 * On drag ennd.
	 * 
	 * @param view
	 *            the view
	 * @param position
	 *            the position
	 * @param object
	 *            the object
	 */
	private void onDragEnnd(View view, int position, Object object) {
		if (!mDragEnded) {
			mDragDropListener.onItemDragEnd(view, position,
					mRecords.get(position));
			mDragEnded = true;
		}
	}

	/**
	 * Sets the row droppable.
	 * 
	 * @param droppable
	 *            the droppable
	 * @param draggableView
	 *            the draggable view
	 */
	public void setRowDroppable(boolean droppable, View draggableView) {
		mRowDroppable = droppable;
		mDraggableView = draggableView;
	}

	/**
	 * Adds the drop listener layout.
	 * 
	 * @param resource
	 *            the resource
	 */
	public void addDropListenerLayout(int resource) {
		mDropLayouts.add(resource);
		ViewGroup view = (ViewGroup) getParent();
		View droppable_view = view.findViewById(resource);
		droppable_view.setTag("droppable_view");
		droppable_view.setOnDragListener(this);
	}

	/**
	 * Checks if is droppable.
	 * 
	 * @param v
	 *            the v
	 * @return true, if is droppable
	 */
	public boolean isDroppable(View v) {
		if (v.getTag() != null
				&& v.getTag().toString().equals("droppable_view"))
			return true;
		return false;
	}

	/**
	 * Gets the draggable view.
	 * 
	 * @return the draggable view
	 */
	public View getDraggableView() {
		return mDraggableView;
	}

	/**
	 * Sets the on list row view click listener.
	 * 
	 * @param view_id
	 *            the view_id
	 * @param listener
	 *            the listener
	 */
	public void setOnListRowViewClickListener(Integer view_id,
			OnListRowViewClickListener listener) {
		mViewClickListener.add(new ViewClickListeners(view_id, listener));
	}

	/**
	 * Sets the before list row create listener.
	 * 
	 * @param callback
	 *            the new before list row create listener
	 */
	public void setBeforeListRowCreateListener(
			BeforeListRowCreateListener callback) {
		mBeforeListRowCreateListener = callback;
	}

	/**
	 * The listener interface for receiving onListRowViewClick events. The class
	 * that is interested in processing a onListRowViewClick event implements
	 * this interface, and the object created with that class is registered with
	 * a component using the component's
	 * <code>addOnListRowViewClickListener<code> method. When
	 * the onListRowViewClick event occurs, that object's appropriate
	 * method is invoked.
	 * 
	 * @see OnListRowViewClickEvent
	 */
	public interface OnListRowViewClickListener {

		/**
		 * On row view click.
		 * 
		 * @param view_group
		 *            the view_group
		 * @param view
		 *            the view
		 * @param position
		 *            the position
		 * @param row
		 *            the row
		 */
		public void onRowViewClick(ViewGroup view_group, View view,
				int position, ODataRow row);
	}

	/**
	 * The Class ViewClickListeners.
	 */
	private class ViewClickListeners {

		/** The _listener_data. */
		private HashMap<String, OnListRowViewClickListener> _listener_data = new HashMap<String, OnListRowViewClickListener>();

		/** The _listener_view. */
		private HashMap<String, Integer> _listener_view = new HashMap<String, Integer>();

		/**
		 * Instantiates a new view click listeners.
		 * 
		 * @param view_id
		 *            the view_id
		 * @param listener
		 *            the listener
		 */
		public ViewClickListeners(Integer view_id,
				OnListRowViewClickListener listener) {
			String key = "KEY_" + view_id;
			_listener_data.put(key, listener);
			_listener_view.put(key, view_id);
		}

		/**
		 * Gets the listener.
		 * 
		 * @param key
		 *            the key
		 * @return the listener
		 */
		public OnListRowViewClickListener getListener(String key) {
			return _listener_data.get(key);
		}

		/**
		 * Gets the view id.
		 * 
		 * @param key
		 *            the key
		 * @return the view id
		 */
		public Integer getViewId(String key) {
			return _listener_view.get(key);
		}

		/**
		 * Gets the keys.
		 * 
		 * @return the keys
		 */
		public List<String> getKeys() {
			List<String> keys = new ArrayList<String>();
			keys.addAll(_listener_view.keySet());
			return keys;
		}
	}

	/**
	 * The listener interface for receiving beforeListRowCreate events. The
	 * class that is interested in processing a beforeListRowCreate event
	 * implements this interface, and the object created with that class is
	 * registered with a component using the component's
	 * <code>addBeforeListRowCreateListener<code> method. When
	 * the beforeListRowCreate event occurs, that object's appropriate
	 * method is invoked.
	 * 
	 * @see BeforeListRowCreateEvent
	 */
	public interface BeforeListRowCreateListener {

		/**
		 * Before list row create.
		 * 
		 * @param position
		 *            the position
		 * @param view
		 *            the view
		 */
		public void beforeListRowCreate(int position, ODataRow row, View view);
	}
}
