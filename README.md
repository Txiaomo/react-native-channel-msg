
# react-native-channel-msg

## Getting started

`$ yarn add react-native-channel-msg`

`$ npm install react-native-channel-msg --save`

### Mostly automatic installation
### 新版本RN自动关联，无需link
`$ react-native link react-native-channel-msg`

### Manual installation


#### Android
1. Open up `android/app/src/main/java/[...]/MainActivity.java`
  - Add `import com.vision.RNChannelPluginPackage;` to the imports at the top of the file
  - Add `new RNChannelPluginPackage()` to the list returned by the `getPackages()` method
2. Append the following lines to `android/settings.gradle`:
  	```
  	include ':react-native-channel-msg'
  	project(':react-native-channel-msg').projectDir = new File(rootProject.projectDir, 	'../node_modules/react-native-channel-msg/android')
  	```
3. Insert the following lines inside the dependencies block in `android/app/build.gradle`:
  	```
      implementation project(':react-native-channel-msg')
  	```

## Usage
```javascript

const {ChannelPlugin} = NativeModules;

/**
 * 创建通知渠道（创建两个或以上有效） 
 * @param id 			渠道id
 * @param name  		渠道名
 * @param importance	重要等级（-1~4） -1用于保留偏好设置，不应与实际通知关联，提示等级依次增强
 * @param id 渠道id
 */ 
ChannelPlugin.createNotificationChannel(String id,String name,Integer importance)

/**
 * 设置通知左侧图标
 * @param iconName	通知左侧图标,可以在发送通知时传入
 */
ChannelPlugin.setSmallIcon(String iconName) ;

/**
 * 设置图标,图标需要放在android/src/main/res/drawable下，传入图标名称即可
 * @param iconName	通知左侧图标,可以在发送通知时传入
 * @param largeIcon	通知内容右侧图片
 */
ChannelPlugin.setIcon(String iconName,String largeIcon);

/**
 * 发送通知
 * @param map  				传入的对象。需包含以下
 * @param contentTitle  	内容标题
 * @param contentText  	 	内容正文
 * @param smallIcon     	消息左侧图标（调用了setIcon方法后可不传）
 * @param msgId  			消息Id（可选，Int类型，区分消息类型,不区分会导致不同的消息会被合并）
 * @param channelId     	渠道ID（可选，渠道消息用）
 * @param nitificationInfo  消息信息（可选，点击消息的时候回传此标识）
 */
ChannelPlugin.sendNotification({contentTitle:"",...})

/**
 * 判断消息通知是否被关闭
 * @param channelId 渠道消息id（可选，传null则判断应用的通知是否被关闭）
 * 重点：渠道通知开启，应用通知关闭的情况下，检测渠道通知的结果为开启
 */
ChannelPlugin.censorNotification(String channelId,info => {
	if (info === 'Normal') {
		//正常
	} else if(info === 'AllClose') {
		//应用通知关闭
	} else if(info === 'close') {
		//当前检测的渠道通知关闭
	}
})

/**
 * 进入应用通知设置
 * @param channelId 渠道Id,跳转到传入的渠道ID通知设置页。传null则跳转到应用的通知设置页 
 */ 
ChannelPlugin.inSettings(String channelId);

// easy game,best game
```
  
