FlashPlayerControl is integrated with Swing via JNI so we can host
the Flash Player inside a Swing window for Photopulse show previews.

FlashPlayerControl commercial Flash Player hosting header/libs should go in this
directory. The following files:

	f_in_box.h
	f_in_box.lib
	functions_decl.inl
	messages.inl
	notification_messages.inl

Also f_in_box.dll should be placed in ../../../../bin
for use at runtime.

http://www.f-in-box.com/dll/
