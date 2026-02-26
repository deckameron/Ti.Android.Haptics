/**
 * Titanium SDK
 * Copyright TiDev, Inc. 04/07/2022-Present
 * Licensed under the terms of the Apache Public License
 * Please see the LICENSE included with this distribution for details.
 */

/** This is generated, do not edit by hand. **/

#include "Proxy.h"

namespace ti {
namespace android {
namespace haptics {

class TiAndroidHapticsModule : public titanium::Proxy
{
public:
	explicit TiAndroidHapticsModule();

	static void bindProxy(v8::Local<v8::Object>, v8::Local<v8::Context>);
	static v8::Local<v8::FunctionTemplate> getProxyTemplate(v8::Isolate*);
	static v8::Local<v8::FunctionTemplate> getProxyTemplate(v8::Local<v8::Context>);
	static void dispose(v8::Isolate*);

	static jclass javaClass;

private:
	static v8::Persistent<v8::FunctionTemplate> proxyTemplate;
	static v8::Persistent<v8::Object> moduleInstance;

	// Methods -----------------------------------------------------------
	static void cancel(const v8::FunctionCallbackInfo<v8::Value>&);
	static void notification(const v8::FunctionCallbackInfo<v8::Value>&);
	static void doubleClick(const v8::FunctionCallbackInfo<v8::Value>&);
	static void selection(const v8::FunctionCallbackInfo<v8::Value>&);
	static void hasAmplitudeControl(const v8::FunctionCallbackInfo<v8::Value>&);
	static void effect(const v8::FunctionCallbackInfo<v8::Value>&);
	static void impact(const v8::FunctionCallbackInfo<v8::Value>&);
	static void isSupported(const v8::FunctionCallbackInfo<v8::Value>&);
	static void waveform(const v8::FunctionCallbackInfo<v8::Value>&);
	static void performHapticFeedback(const v8::FunctionCallbackInfo<v8::Value>&);
	static void oneShot(const v8::FunctionCallbackInfo<v8::Value>&);

	// Dynamic property accessors ----------------------------------------

};

} // haptics
} // android
} // ti
