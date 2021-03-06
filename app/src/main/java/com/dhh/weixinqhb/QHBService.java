package com.dhh.weixinqhb;

import android.accessibilityservice.AccessibilityService;
import android.annotation.TargetApi;
import android.app.Notification;
import android.app.PendingIntent;
import android.os.Build;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dhh on 2016/12/22.
 */
public class QHBService extends AccessibilityService {

    private List<AccessibilityNodeInfo> parents;
    private boolean isHaveOpen;
    private String lastClassName = "";
    private String LuckyMoneyReceiveUI;
    private String LauncherUI;
    private String LuckyMoneyDetailUI;

    /**
     * 当启动服务的时候就会被调用
     */
    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        parents = new ArrayList<>();
        LauncherUI = "com.tencent.mm.ui.LauncherUI";
        LuckyMoneyReceiveUI = "com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyReceiveUI";
        LuckyMoneyDetailUI = "com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyDetailUI";
    }


    /**
     * 监听窗口变化的回调
     */
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        int eventType = event.getEventType();
        switch (eventType) {
            //当通知栏发生改变时
            case AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED:
                List<CharSequence> texts = event.getText();
                if (!texts.isEmpty()) {
                    for (CharSequence text : texts) {
                        String content = text.toString();
                        if (content.contains("[微信红包]")) {
                            //模拟打开通知栏消息，即打开微信
                            if (event.getParcelableData() != null &&
                                    event.getParcelableData() instanceof Notification) {
                                Notification notification = (Notification) event.getParcelableData();
                                PendingIntent pendingIntent = notification.contentIntent;
                                try {
                                    lastClassName = "";
                                    pendingIntent.send();
                                    Log.e("demo", "进入微信");
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }
                break;
            //当窗口的状态发生改变时
            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
                String className = event.getClassName().toString();
                if (className.equals(LauncherUI)) {
                    if (lastClassName.equals(LuckyMoneyDetailUI)) {
                        break;
                    }
                    //点击最后一个红包
                    Log.e("demo", "点击红包");
                    getLastPacket();
                    lastClassName = LauncherUI;
                } else if (className.equals(LuckyMoneyReceiveUI)) {
                    //开红包
                    Log.e("demo", "开红包");
                    inputClick("com.tencent.mm:id/bg7");
                } else if (className.equals(LuckyMoneyDetailUI)) {
                    //退出红包
                    Log.e("demo", "退出红包");
                    inputClick("com.tencent.mm:id/gd");
                    lastClassName = LuckyMoneyDetailUI;
//                    isHaveOpen = true;
                } else {
                    lastClassName = className;
                }
                break;
        }
    }

    /**
     * 通过ID获取控件，并进行模拟点击
     *
     * @param clickId
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void inputClick(String clickId) {
        AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
        if (nodeInfo != null) {
            List<AccessibilityNodeInfo> list = nodeInfo.findAccessibilityNodeInfosByViewId(clickId);
            for (AccessibilityNodeInfo item : list) {
                item.performAction(AccessibilityNodeInfo.ACTION_CLICK);
            }
        }
    }

    /**
     * 获取List中最后一个红包，并进行模拟点击
     */
    private void getLastPacket() {
        AccessibilityNodeInfo rootNode = getRootInActiveWindow();
        if (rootNode == null) {
            return;
        }
        recycle(rootNode, "领取红包");
        if (parents.size() > 0) {
            parents.get(parents.size() - 1).performAction(AccessibilityNodeInfo.ACTION_CLICK);
        }
    }

    /**
     * 回归函数遍历每一个节点，并将含有"领取红包"存进List中
     *
     * @param info
     */
    public void recycle(AccessibilityNodeInfo info, String keyWord) {
        if (info.getChildCount() == 0) {
            if (info.getText() != null) {
                Log.d("QHBService", "info.getText():" + info.getText());
                if (info.getText().equals("你领取了")) {
                    isHaveOpen = true;
                }
                if (keyWord.equals(info.getText().toString())) {
                    if (info.isClickable()) {
                        info.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    }
                    AccessibilityNodeInfo parent = info.getParent();
                    while (parent != null) {
                        if (parent.isClickable()) {
                            parents.add(parent);
                            break;
                        }
                        parent = parent.getParent();
                    }
                }
            }
        } else {
            for (int i = 0; i < info.getChildCount(); i++) {
                if (info.getChild(i) != null) {
                    recycle(info.getChild(i), keyWord);
                }
            }
        }
    }

    /**
     * 中断服务的回调
     */
    @Override
    public void onInterrupt() {

    }
}
