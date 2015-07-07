package zce.app.python.compile.widget;

import zce.app.python.compile.MainActivity;
import zce.app.python.compile.util.Settings;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class EditText extends android.widget.EditText {

	/**
	 * 清空按钮
	 */
	private Drawable cDrawable;

	/**
	 * 打开文件按钮
	 */
	private Drawable oDrawable;

	/**
	 * 控件是否有焦点
	 */
	private boolean hasFoucs;

	private String tag;

	public EditText(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public EditText(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	public EditText(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
		// 默认隐藏清除图片
		setClearIconVisible(false);
	}

	@Override
	public void setCompoundDrawables(Drawable left, Drawable top,
			Drawable right, Drawable bottom) {
		// TODO Auto-generated method stub
		if (left != null && oDrawable == null) {
			oDrawable = left;
		}
		if (right != null && cDrawable == null) {
			cDrawable = right;
		}
		super.setCompoundDrawables(left, top, right, bottom);
	}

	/**
	 * 因为不能直接给EditText设置点击事件 所以用记住按下的位置来模拟点击事件 当按下的位置 在 EditText的宽度 - 图标到控件右边的间距
	 * - 图标的宽度 和 EditText的宽度 - 图标到控件右边的间距之间就算点击了图标 竖直方向就没有考虑
	 */
	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub
		if (event.getAction() == MotionEvent.ACTION_UP) {
			float x = event.getX();
			int right = getRight();
			int left = getLeft();
			if ((x > (right - getTotalPaddingRight() - getPaddingRight()))
					&& (x < right)) {// 432<x<480
				setText("");
			} else if ((x > left)
					&& (x < (getTotalPaddingLeft() - getPaddingLeft()))) {// 0<x<48
				Intent intent = new Intent(
						getContext().getApplicationContext(),
						MainActivity.class);
				intent.setAction("zce.app.python.compile.OPENFILE");
				intent.putExtra("which", tag);// 表明是图标路径还是文件路径
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				getContext().getApplicationContext().startActivity(intent);
			}
		}
		return super.onTouchEvent(event);
	}

	/**
	 * 当EditText焦点发生变化的时候，判断里面字符串长度设置清除图标的显示与隐藏
	 */
	@Override
	protected void onFocusChanged(boolean focused, int direction,
			Rect previouslyFocusedRect) {
		// TODO Auto-generated method stub
		this.setHasFoucs(focused);
		if (focused) {
			setClearIconVisible(getText().length() > 0);
		} else {
			// 失去焦点时保存
			setClearIconVisible(false);
			Settings.saveSettings(
					getContext().getApplicationContext().getSharedPreferences(
							getContext().getApplicationContext()
									.getPackageName(), Context.MODE_PRIVATE),
					tag, getText().toString().trim());
		}
		super.onFocusChanged(focused, direction, previouslyFocusedRect);
	}

	/**
	 * 设置清除图标的显示与隐藏，调用setCompoundDrawables为EditText绘制上去
	 * 
	 * @param visible
	 */
	protected void setClearIconVisible(boolean visible) {
		Drawable right = visible ? cDrawable : null;
		setCompoundDrawables(getCompoundDrawables()[0],
				getCompoundDrawables()[1], right, getCompoundDrawables()[3]);
	}

	/**
	 * 当输入框里面内容发生变化的时候回调的方法
	 */
	@Override
	public void onTextChanged(CharSequence s, int start, int count, int after) {
		setClearIconVisible(s.length() > 0);
	}

	public boolean getHasFoucs() {
		return hasFoucs;
	}

	public void setHasFoucs(boolean hasFoucs) {
		this.hasFoucs = hasFoucs;
	}

	@Override
	public void setTag(Object tag) {
		// TODO Auto-generated method stub
		this.tag = (String) tag;
		super.setTag(tag);
	}

}
