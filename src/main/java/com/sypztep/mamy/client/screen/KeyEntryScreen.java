//package com.sypztep.mamy.client.screen;
//
//import com.sypztep.mamy.client.MamyClient;
//import net.minecraft.client.MinecraftClient;
//import net.minecraft.client.gui.DrawContext;
//import net.minecraft.client.gui.screen.Screen;
//import net.minecraft.client.gui.widget.TextFieldWidget;
//import net.minecraft.text.Text;
//
//import com.sypztep.mamy.common.DatabaseManager;
//
//import net.minecraft.util.Formatting;
//import org.lwjgl.glfw.GLFW;
//
//import java.net.NetworkInterface;
//import java.util.Collections;
//
//public class KeyEntryScreen extends Screen {
//    private TextFieldWidget keyInput;
//    private Text feedbackMessage = Text.empty();
//    private long lastValidationTime = 0;
//    private static final long VALIDATION_COOLDOWN_MS = 5000; // 5 seconds
//
//    public KeyEntryScreen() {
//        super(Text.literal("Enter Access Key"));
//    }
//
//    @Override
//    protected void init() {
//        int centerX = width / 2;
//        int centerY = height / 2;
//
//        keyInput = new TextFieldWidget(textRenderer, centerX - 100, centerY - 10, 200, 20, Text.literal("Access Key"));
//        keyInput.setMaxLength(64);
//        addSelectableChild(keyInput);
//
//        setInitialFocus(keyInput);
//    }
//
//    @Override
//    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
//        if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
//            String enteredKey = keyInput.getText().trim();
//            if (!enteredKey.isEmpty()) {
//                long now = System.currentTimeMillis();
//                if (now - lastValidationTime < VALIDATION_COOLDOWN_MS) {
//                    feedbackMessage = Text.literal("Please wait before trying again.");
//                } else {
//                    lastValidationTime = now;
//                    onSubmitKey(enteredKey);
//                }
//                return true;
//            }
//        }
//        return super.keyPressed(keyCode, scanCode, modifiers);
//    }
//
//    private void onSubmitKey(String key) {
//        MinecraftClient client = MinecraftClient.getInstance();
//        String identity = getPlayerIdentity(client);
//
//        feedbackMessage = Text.literal("Validating key, please wait...");
//
//        new Thread(() -> {
//            try {
//                if (DatabaseManager.connection == null || DatabaseManager.connection.isClosed()) {
//                    DatabaseManager.connect();
//                }
//
//                boolean valid = DatabaseManager.validateAndAssignKey(key, identity);
//
//                client.execute(() -> {
//                    if (valid) {
//                        feedbackMessage = Text.literal("Key valid! Access granted.").formatted(Formatting.GREEN;
//
//                        // Delay closing screen so player sees message
//                        new Thread(() -> {
//                            try {
//                                Thread.sleep(1500); // 1.5 seconds delay
//                            } catch (InterruptedException ignored) {}
//                            client.execute(() -> client.setScreen(null));
//                        }).start();
//
//                    } else {
//                        feedbackMessage = Text.literal("Invalid or already used key.");
//                        keyInput.setText("");
//                    }
//                });
//            } catch (Exception e) {
//                client.execute(() -> feedbackMessage = Text.literal("Error validating key. Try again later."));
//                e.printStackTrace();
//            }
//        }).start();
//    }
//
//    private String getPlayerIdentity(MinecraftClient client) {
//        try {
//            String username = client.getGameProfile().getName();
//            String hwid = getHWID();
//            String identity = username + "_" + hwid;
//            if(identity.length() > 64) {
//                identity = identity.substring(0, 64);
//            }
//            return identity;
//        } catch (Exception e) {
//            return "unknown_identity";
//        }
//    }
//
//    private String getHWID() {
//        try {
//            for (NetworkInterface ni : Collections.list(NetworkInterface.getNetworkInterfaces())) {
//                byte[] mac = ni.getHardwareAddress();
//                if (mac != null) {
//                    StringBuilder sb = new StringBuilder();
//                    for (byte b : mac) {
//                        sb.append(String.format("%02X", b));
//                    }
//                    return sb.toString();
//                }
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return "UNKNOWN_HWID";
//    }
//
//    @Override
//    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
//        super.render(context, mouseX, mouseY, delta);
//        context.drawCenteredTextWithShadow(textRenderer, getTitle(), width / 2, height / 2 - 40, 0xFFFFFF);
//        keyInput.render(context, mouseX, mouseY, delta);
//        int feedbackY = (height / 2) + 15;
//        context.drawCenteredTextWithShadow(textRenderer, feedbackMessage, width / 2, feedbackY, 0xFF5555);
//    }
//
//    @Override
//    public boolean shouldCloseOnEsc() {
//        return false;
//    }
//}
