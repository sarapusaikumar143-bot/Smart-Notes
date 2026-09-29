const apiKey = (process.env.GEMINI_API_KEY || (import.meta as any).env?.VITE_GEMINI_API_KEY || '') as string;

export interface LiveMessage {
  id: string;
  sender: 'user' | 'agent';
  text: string;
  timestamp: number;
}

export class GeminiLiveSession {
  private ws: WebSocket | null = null;
  private audioContext: AudioContext | null = null;
  private mediaStream: MediaStream | null = null;
  private processor: ScriptProcessorNode | null = null;
  private isConnected = false;
  private onMessageCallback: (msg: LiveMessage) => void;
  private onStatusChangeCallback: (status: 'connecting' | 'connected' | 'speaking' | 'disconnected' | 'error') => void;

  constructor(
    onMessage: (msg: LiveMessage) => void,
    onStatusChange: (status: 'connecting' | 'connected' | 'speaking' | 'disconnected' | 'error') => void
  ) {
    this.onMessageCallback = onMessage;
    this.onStatusChangeCallback = onStatusChange;
  }

  async start(financialSummary: string): Promise<void> {
    this.onStatusChangeCallback('connecting');
    const currentKey = apiKey || (process.env.GEMINI_API_KEY || (import.meta as any).env?.VITE_GEMINI_API_KEY || '');

    if (!currentKey || currentKey === 'MY_GEMINI_API_KEY') {
      this.onStatusChangeCallback('error');
      throw new Error('Gemini API key is not configured');
    }

    try {
      this.mediaStream = await navigator.mediaDevices.getUserMedia({ audio: true });
      this.audioContext = new (window.AudioContext || (window as any).webkitAudioContext)({ sampleRate: 16000 });

      // Live API endpoint for gemini-3.8-live
      const wsUrl = `wss://generativelanguage.googleapis.com/ws/google.ai.generativelanguage.v1alpha.GenerativeService.BidiGenerateContent?key=${currentKey}`;
      
      this.ws = new WebSocket(wsUrl);

      this.ws.onopen = () => {
        this.isConnected = true;
        this.onStatusChangeCallback('connected');

        // Initial setup frame with model gemini-3.8-live
        const setupMessage = {
          setup: {
            model: "models/gemini-3.8-live",
            generationConfig: {
              responseModalities: ["AUDIO", "TEXT"],
              speechConfig: {
                voiceConfig: {
                  prebuiltVoiceConfig: {
                    voiceName: "Aoede"
                  }
                }
              }
            },
            systemInstruction: {
              parts: [{
                text: `You are SpendWise Live AI, a real-time conversational voice financial copilot.
User's Financial Profile:
${financialSummary}

Keep your voice answers conversational, concise (1-2 sentences), warm, and direct. Listen attentively and answer questions on expenses, savings, and financial habits.`
              }]
            }
          }
        };

        this.ws?.send(JSON.stringify(setupMessage));
        this.startMicrophoneStream();
      };

      this.ws.onmessage = async (event) => {
        try {
          let dataStr = typeof event.data === 'string' ? event.data : await (event.data as Blob).text();
          const response = JSON.parse(dataStr);

          // Handle server content
          const parts = response.serverContent?.modelTurn?.parts;
          if (parts && parts.length > 0) {
            for (const part of parts) {
              if (part.text) {
                this.onMessageCallback({
                  id: `live_${Date.now()}`,
                  sender: 'agent',
                  text: part.text,
                  timestamp: Date.now()
                });
                this.onStatusChangeCallback('speaking');
              }
              // If base64 PCM audio is received
              if (part.inlineData?.data) {
                this.playPcmAudio(part.inlineData.data);
              }
            }
          }

          if (response.serverContent?.turnComplete) {
            this.onStatusChangeCallback('connected');
          }
        } catch (e) {
          console.warn('Error handling live message:', e);
        }
      };

      this.ws.onerror = (err) => {
        console.warn('WebSocket live error, switching to fallback voice mode:', err);
        this.fallbackVoiceMode(financialSummary);
      };

      this.ws.onclose = () => {
        this.isConnected = false;
        this.onStatusChangeCallback('disconnected');
      };
    } catch (e) {
      console.warn('Live API connection failed, using browser voice fallback:', e);
      this.fallbackVoiceMode(financialSummary);
    }
  }

  private startMicrophoneStream() {
    if (!this.audioContext || !this.mediaStream) return;
    const source = this.audioContext.createMediaStreamSource(this.mediaStream);
    this.processor = this.audioContext.createScriptProcessor(4096, 1, 1);

    this.processor.onaudioprocess = (e) => {
      if (!this.isConnected || !this.ws || this.ws.readyState !== WebSocket.OPEN) return;
      const inputData = e.inputBuffer.getChannelData(0);
      const pcm16 = this.floatTo16BitPCM(inputData);
      const base64 = this.arrayBufferToBase64(pcm16.buffer);

      const audioMsg = {
        realtimeInput: {
          mediaChunks: [{
            mimeType: "audio/pcm;rate=16000",
            data: base64
          }]
        }
      };
      this.ws.send(JSON.stringify(audioMsg));
    };

    source.connect(this.processor);
    this.processor.connect(this.audioContext.destination);
  }

  private floatTo16BitPCM(input: Float32Array): Int16Array {
    const output = new Int16Array(input.length);
    for (let i = 0; i < input.length; i++) {
      const s = Math.max(-1, Math.min(1, input[i]));
      output[i] = s < 0 ? s * 0x8000 : s * 0x7FFF;
    }
    return output;
  }

  private arrayBufferToBase64(buffer: ArrayBuffer): string {
    let binary = '';
    const bytes = new Uint8Array(buffer);
    const len = bytes.byteLength;
    for (let i = 0; i < len; i++) {
      binary += String.fromCharCode(bytes[i]);
    }
    return window.btoa(binary);
  }

  private playPcmAudio(base64: string) {
    if (!this.audioContext) return;
    try {
      const binaryString = window.atob(base64);
      const len = binaryString.length;
      const bytes = new Uint8Array(len);
      for (let i = 0; i < len; i++) {
        bytes[i] = binaryString.charCodeAt(i);
      }
      const int16 = new Int16Array(bytes.buffer);
      const float32 = new Float32Array(int16.length);
      for (let i = 0; i < int16.length; i++) {
        float32[i] = int16[i] / 32768.0;
      }

      const audioBuffer = this.audioContext.createBuffer(1, float32.length, 24000);
      audioBuffer.getChannelData(0).set(float32);

      const source = this.audioContext.createBufferSource();
      source.buffer = audioBuffer;
      source.connect(this.audioContext.destination);
      source.start();
    } catch (e) {
      console.error('Audio playback error:', e);
    }
  }

  // Resilient live speech fallback mode
  private fallbackVoiceMode(financialSummary: string) {
    this.onStatusChangeCallback('connected');
    const SpeechRecognition = (window as any).SpeechRecognition || (window as any).webkitSpeechRecognition;

    if (SpeechRecognition) {
      const recognition = new SpeechRecognition();
      recognition.continuous = true;
      recognition.interimResults = false;
      recognition.lang = 'en-IN';

      recognition.onresult = async (event: any) => {
        const text = event.results[event.results.length - 1][0].transcript;
        if (!text.trim()) return;

        this.onMessageCallback({
          id: `live_u_${Date.now()}`,
          sender: 'user',
          text,
          timestamp: Date.now()
        });

        this.onStatusChangeCallback('speaking');

        // Ask gemini-3.8-live / REST
        const currentKey = apiKey || (process.env.GEMINI_API_KEY || (import.meta as any).env?.VITE_GEMINI_API_KEY || '');
        try {
          const res = await fetch(`https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=${currentKey}`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
              contents: [{ parts: [{ text }] }],
              systemInstruction: {
                parts: [{ text: `You are SpendWise Live Voice Agent. Keep your reply to 1-2 conversational sentences. User data: ${financialSummary}` }]
              }
            })
          });

          const json = await res.json();
          const reply = json.candidates?.[0]?.content?.parts?.[0]?.text || "I'm listening. How can I assist with your budget today?";
          
          this.onMessageCallback({
            id: `live_a_${Date.now()}`,
            sender: 'agent',
            text: reply,
            timestamp: Date.now()
          });

          // Speak back with browser speech synthesis
          if (window.speechSynthesis) {
            const utterance = new SpeechSynthesisUtterance(reply);
            utterance.rate = 1.05;
            utterance.onend = () => this.onStatusChangeCallback('connected');
            window.speechSynthesis.speak(utterance);
          } else {
            this.onStatusChangeCallback('connected');
          }
        } catch (err) {
          this.onStatusChangeCallback('connected');
        }
      };

      try {
        recognition.start();
      } catch (e) {
        // already started
      }
    }
  }

  stop() {
    this.isConnected = false;
    this.ws?.close();
    this.ws = null;
    this.mediaStream?.getTracks().forEach(track => track.stop());
    this.mediaStream = null;
    this.processor?.disconnect();
    this.processor = null;
    this.audioContext?.close();
    this.audioContext = null;
    if (window.speechSynthesis) {
      window.speechSynthesis.cancel();
    }
    this.onStatusChangeCallback('disconnected');
  }
}
