package odoo.controls;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ListView;

import com.odoo.crm.R;

public class OCollectionView extends ListView {

	public final String KEY_EMPTY_LIST_ICON = "emptyIcon";
	public final String KEY_EMPTY_LIST_MESSAGE = "emptyMessage";
	private Context mContext;
	private LayoutInflater mInflater;
	private OControlAttributes mAttr;
	private TypedArray mTypedArray;

	public OCollectionView(Context context) {
		this(context, null);
	}

	public OCollectionView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public OCollectionView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mContext = context;
		mInflater = LayoutInflater.from(mContext);
		mAttr = new OControlAttributes();
		setDivider(null);
		setDividerHeight(0);
		setItemsCanFocus(false);
		setChoiceMode(ListView.CHOICE_MODE_NONE);
		setSelector(android.R.color.transparent);
		setSmoothScrollbarEnabled(true);
		if (attrs != null) {
			mTypedArray = mContext.obtainStyledAttributes(attrs,
					R.styleable.OCollectionView);
			mAttr.put(KEY_EMPTY_LIST_ICON, mTypedArray.getResourceId(
					R.styleable.OCollectionView_emptyIcon,
					R.drawable.ic_action_exclamation_mark));
			mAttr.put(KEY_EMPTY_LIST_MESSAGE, mTypedArray
					.getString(R.styleable.OCollectionView_emptyMessage));
			mTypedArray.recycle();
		}
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}

	public void setEmptyIcon(int resid) {
		mAttr.put(KEY_EMPTY_LIST_ICON, resid);
	}

	public void setEmptyMessage(int resid) {
		mAttr.put(KEY_EMPTY_LIST_MESSAGE, resid);
	}

	public void setEmptyIconMessage(int icon_resId, int message_resId) {
		setEmptyIcon(icon_resId);
		setEmptyMessage(message_resId);
	}

}
