import { GoogleGenAI } from '@google/genai';

const apiKey = (process.env.GEMINI_API_KEY || (import.meta as any).env?.VITE_GEMINI_API_KEY || '') as string;

export class AudioRecorder {
  private mediaRecorder: MediaRecorder | null = null;
  private audioChunks: Blob[] = [];
  private stream: MediaStream | null = null;

  async start(): Promise<void> {
    this.audioChunks = [];
    this.stream = await navigator.mediaDevices.getUserMedia({ audio: true });
    
    // Choose appropriate mime type supported by browser
    let mimeType = 'audio/webm';
    if (!MediaRecorder.isTypeSupported(mimeType)) {
      if (MediaRecorder.isTypeSupported('audio/mp4')) mimeType = 'audio/mp4';
      else if (MediaRecorder.isTypeSupported('audio/ogg')) mimeType = 'audio/ogg';
      else mimeType = '';
    }

    this.mediaRecorder = mimeType ? new MediaRecorder(this.stream, { mimeType }) : new MediaRecorder(this.stream);

    this.mediaRecorder.ondataavailable = (event) => {
      if (event.data.size > 0) {
        this.audioChunks.push(event.data);
      }
    };

    this.mediaRecorder.start(100); // 100ms slices
  }

  async stop(): Promise<{ blob: Blob; base64: string; mimeType: string }> {
    return new Promise((resolve, reject) => {
      if (!this.mediaRecorder) {
        return reject(new Error('MediaRecorder not initialized'));
      }

      this.mediaRecorder.onstop = async () => {
        try {
          const mimeType = this.mediaRecorder?.mimeType || 'audio/webm';
          const audioBlob = new Blob(this.audioChunks, { type: mimeType });

          // Clean up stream tracks
          this.stream?.getTracks().forEach(track => track.stop());
          this.stream = null;

          const base64 = await this.blobToBase64(audioBlob);
          resolve({ blob: audioBlob, base64, mimeType });
        } catch (e) {
          reject(e);
        }
      };

      this.mediaRecorder.stop();
    });
  }

  private blobToBase64(blob: Blob): Promise<string> {
    return new Promise((resolve, reject) => {
      const reader = new FileReader();
      reader.onloadend = () => {
        const result = reader.result as string;
        // Strip data:audio/xxx;base64, prefix
        const base64 = result.split(',')[1] || result;
        resolve(base64);
      };
      reader.onerror = reject;
      reader.readAsDataURL(blob);
    });
  }
}

/**
 * Transcribes audio using model gemini-3.5-transcribe
 */
export async function transcribeAudioWithGemini(base64Audio: string, mimeType: string = 'audio/webm'): Promise<string> {
  const currentKey = apiKey || (process.env.GEMINI_API_KEY || (import.meta as any).env?.VITE_GEMINI_API_KEY || '');
  
  if (!currentKey || currentKey === 'MY_GEMINI_API_KEY') {
    throw new Error('Gemini API key is not configured');
  }

  try {
    const ai = new GoogleGenAI({ apiKey: currentKey });
    
    // Explicit model requirement: gemini-3.5-transcribe
    const response = await ai.models.generateContent({
      model: 'gemini-3.5-transcribe',
      contents: [
        {
          inlineData: {
            mimeType: mimeType.split(';')[0] || 'audio/webm',
            data: base64Audio
          }
        },
        {
          text: 'Transcribe this spoken audio accurately. Output only the verbatim transcription without commentary.'
        }
      ]
    });

    const text = response.text?.trim() || '';
    return text;
  } catch (err: any) {
    console.warn('Gemini 3.5 Transcribe API call failed, falling back to REST/Direct:', err);

    // REST fallback for gemini-3.5-transcribe
    try {
      const endpoint = `https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-transcribe:generateContent?key=${currentKey}`;
      const res = await fetch(endpoint, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          contents: [{
            parts: [
              {
                inlineData: {
                  mimeType: mimeType.split(';')[0] || 'audio/webm',
                  data: base64Audio
                }
              },
              {
                text: 'Transcribe this spoken audio accurately. Output only the verbatim transcription without commentary.'
              }
            ]
          }]
        })
      });

      if (res.ok) {
        const json = await res.json();
        const text = json.candidates?.[0]?.content?.parts?.[0]?.text;
        if (text) return text.trim();
      }
    } catch (restErr) {
      console.error('REST transcribe fallback failed:', restErr);
    }

    throw err;
  }
}
