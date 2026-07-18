import { defineConfig } from "vite";
import react, { reactCompilerPreset } from "@vitejs/plugin-react";
import babel from "@rolldown/plugin-babel";

// https://vite.dev/config/
export default defineConfig({
	plugins: [react(), babel({ presets: [reactCompilerPreset()] })],
	server: {
		port: 5173,
		strictPort: true,
		watch: {
			usePolling: true,
			interval: 1000,
			ignored: ["**/node_modules/**", "**/dist/**", "**/target/**"],
		},
	},
});
