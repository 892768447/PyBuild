package zce.app.python.compile.widget;

import zce.app.python.compile.R;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class ErrorDialog extends AlertDialog implements OnClickListener {

	private TextView textView;
	private Button btn_ask, btn_cancel;

	@SuppressLint("InflateParams")
	public ErrorDialog(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		View view = getLayoutInflater().inflate(R.layout.error_dialog, null);
		setTitle("错误日志");
		setIcon(R.drawable.ic_launcher);
		setView(view);
		textView = (TextView) view.findViewById(R.id.error_text);
		btn_ask = (Button) view.findViewById(R.id.btn_ask);
		btn_ask.setOnClickListener(this);
		btn_cancel = (Button) view.findViewById(R.id.btn_cancel);
		btn_cancel.setOnClickListener(this);
	}

	protected ErrorDialog(Context context, boolean cancelable,
			OnCancelListener cancelListener) {
		super(context, cancelable, cancelListener);
		// TODO Auto-generated constructor stub
	}

	protected ErrorDialog(Context context, int theme) {
		super(context, theme);
		// TODO Auto-generated constructor stub
	}

	public void setText(String text) {
		textView.setText(text);
	}

	private String getText() {
		return textView.getText().toString();
	}

	@Override
	public void onClick(View view) {
		// TODO Auto-generated method stub
		switch (view.getId()) {
		case R.id.btn_ask:
			Intent intent = new Intent(Intent.ACTION_SEND);
			intent.setType("plain/text");
			intent.putExtra(Intent.EXTRA_EMAIL,
					new String[] { "892768447@qq.com" });// 收件人
			intent.putExtra(Intent.EXTRA_SUBJECT, "PyBuild 编译失败报告");// 主题
			intent.putExtra(Intent.EXTRA_TEXT, getText());
			Intent.createChooser(intent, "PyBuild 编译失败报告");
			getContext().startActivity(intent);
			break;
		case R.id.btn_cancel:
			this.dismiss();
			break;
		}
	}

}
