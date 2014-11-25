package odoo.controls.helper;

import android.view.View;
import android.widget.AbsListView;

public class OListViewUtil {
	public static View getViewFromListView(AbsListView list, int position) {
		final int firstListItemPosition = list.getFirstVisiblePosition();
		final int lastListItemPosition = firstListItemPosition
				+ list.getChildCount() - 1;

		if (position < firstListItemPosition || position > lastListItemPosition) {
			return list.getAdapter().getView(position, null, list);
		} else {
			final int childIndex = position - firstListItemPosition;
			return list.getChildAt(childIndex);
		}
	}

}
