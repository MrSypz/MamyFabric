package com.sypztep.mamy.client.screen;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.lwjgl.glfw.GLFW;

import java.net.NetworkInterface;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class KeyEntryScreen extends Screen {
    private TextFieldWidget keyInput;
    private Text feedbackMessage = Text.empty();
    private long lastValidationTime = 0;
    private static final long VALIDATION_COOLDOWN_MS = 5000; // 5 seconds

    public static boolean keyValidated = false;

    public KeyEntryScreen() {
        super(Text.literal("Enter Access Key"));
    }

    @Override
    protected void init() {
        int centerX = width / 2;
        int centerY = height / 2;
        int widgetWidth = 100; // or 80 as your code
        keyInput = new TextFieldWidget(textRenderer, centerX - (widgetWidth / 2), centerY - 10, widgetWidth, 20, Text.literal("Access Key"));
        keyInput.setMaxLength(32);
        addSelectableChild(keyInput);

        setInitialFocus(keyInput);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
            String enteredKey = keyInput.getText().trim();
            if (!enteredKey.isEmpty()) {
                long now = System.currentTimeMillis();
                if (now - lastValidationTime < VALIDATION_COOLDOWN_MS) {
                    feedbackMessage = Text.literal("Please wait before trying again.").formatted(Formatting.RED);
                } else {
                    lastValidationTime = now;
                    onSubmitKey(enteredKey);
                }
                return true;
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private void onSubmitKey(String token) {
        MinecraftClient client = MinecraftClient.getInstance();
        String identity = getPlayerIdentity(client); // e.g. "username_HWID"

        feedbackMessage = Text.literal("Validating token, please wait...").formatted(Formatting.YELLOW);

        // Replace with your actual API URL and expected params
        String apiUrl = String.format("http://tyranus.online/smp/validate_token.php?token=%s&uuid=%s",
                token, identity);

        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .GET()
                .build();

        CompletableFuture<HttpResponse<String>> responseFuture = httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString());

        responseFuture.thenAccept(response -> {
            try {
                JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
                boolean success = json.get("success").getAsBoolean();
                String message = json.get("message").getAsString();

                client.execute(() -> {
                    feedbackMessage = Text.literal(message);
                    if (success) {
                        keyValidated = true;
                        client.setScreen(null); // Close screen to allow play
                    }
                });
            } catch (Exception e) {
                client.execute(() -> feedbackMessage = Text.literal("Failed to parse server response.").formatted(Formatting.RED));
                e.printStackTrace();
            }
        }).exceptionally(ex -> {
            client.execute(() -> feedbackMessage = Text.literal("Failed to contact server.").formatted(Formatting.RED));
            ex.printStackTrace();
            return null;
        });
    }

    public static String getPlayerIdentity(MinecraftClient client) {
        try {
            String username = client.getGameProfile().getName();
            String hwid = getHWID();
            String identity = username + "_" + hwid;
            if (identity.length() > 64) {
                identity = identity.substring(0, 64);
            }
            return identity;
        } catch (Exception e) {
            return "unknown_identity";
        }
    }

    private static String getHWID() {
        try {
            for (NetworkInterface ni : Collections.list(NetworkInterface.getNetworkInterfaces())) {
                byte[] mac = ni.getHardwareAddress();
                if (mac != null) {
                    StringBuilder sb = new StringBuilder();
                    for (byte b : mac) {
                        sb.append(String.format("%02X", b));
                    }
                    return sb.toString();
                }
            }
        } catch (Exception ignored) {
        }
        return "UNKNOWN_HWID";
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(textRenderer, getTitle(), width / 2, height / 2 - 40, 0xFFFFFF);
        keyInput.render(context, mouseX, mouseY, delta);
        int feedbackY = (height / 2) + 15;
        context.drawCenteredTextWithShadow(textRenderer, feedbackMessage, width / 2, feedbackY, 0xFF5555);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

}
