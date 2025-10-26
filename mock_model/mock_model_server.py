"""
Mock Model Server for ChatPA Testing

This server simulates the model side of the ChatPA system for testing purposes.
It receives requests via WebSocket and responds with mock AI-generated text.

Usage:
    python mock_model_server.py

Default port: 8081 (configurable below)
"""

import asyncio
import json
import logging
from typing import List, Dict, Any
import random
import time

try:
    import websockets
    from websockets.server import WebSocketServerProtocol
except ImportError:
    print("Error: websockets library not installed")
    print("Please install it with: pip install websockets")
    exit(1)

# Configuration
HOST = "0.0.0.0"
PORT = 8081
LOG_LEVEL = logging.INFO

# Setup logging
logging.basicConfig(
    level=LOG_LEVEL,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger(__name__)


class MockModelServer:
    """Mock model server that simulates AI responses"""
    
    # Mock responses for testing
    MOCK_RESPONSES = [
        "根据您的问题，我可以提供以下信息：这是一个模拟的AI响应，用于测试系统功能。",
        "经过分析，我认为最好的解决方案是采用渐进式的方法来处理这个问题。首先，我们需要明确目标...",
        "让我详细解释一下这个概念。从技术角度来看，这涉及到多个层面的考虑，包括性能、安全性和可维护性。",
        "这是一个很好的问题！让我从几个不同的角度来分析：第一，从理论层面；第二，从实践层面；第三，从长远发展来看...",
        "我理解您的需求。针对这个情况，我建议采取以下步骤：1) 评估现状 2) 制定计划 3) 逐步实施 4) 持续优化。",
    ]
    
    MOCK_SOURCES = [
        ["文档A.pdf", "参考资料B.docx"],
        ["研究论文C.pdf", "技术手册D.md", "案例分析E.txt"],
        ["知识库条目F", "官方文档G"],
        ["学术论文H.pdf", "技术博客I"],
        ["内部文档J.docx"],
    ]
    
    MOCK_SUMMARIES = [
        "本次对话主要讨论了技术实现方案和最佳实践。",
        "对话涉及系统架构设计、性能优化和安全考虑等多个方面。",
        "主要话题包括问题分析、解决方案评估和实施建议。",
        "讨论集中在具体技术细节和实际应用场景。",
    ]
    
    def __init__(self):
        self.active_connections: Dict[WebSocketServerProtocol, str] = {}
        
    async def handle_client(self, websocket: WebSocketServerProtocol, path: str):
        """Handle a WebSocket client connection"""
        client_id = f"{websocket.remote_address[0]}:{websocket.remote_address[1]}"
        self.active_connections[websocket] = client_id
        logger.info(f"New connection from {client_id}")
        
        try:
            async for message in websocket:
                await self.process_message(websocket, message, client_id)
        except websockets.exceptions.ConnectionClosed:
            logger.info(f"Connection closed: {client_id}")
        except Exception as e:
            logger.error(f"Error handling client {client_id}: {e}", exc_info=True)
        finally:
            if websocket in self.active_connections:
                del self.active_connections[websocket]
                logger.info(f"Removed connection: {client_id}")
    
    async def process_message(self, websocket: WebSocketServerProtocol, message: str, client_id: str):
        """Process incoming message from client"""
        try:
            data = json.loads(message)
            logger.info(f"Received from {client_id}: {json.dumps(data, ensure_ascii=False)[:200]}")
            
            # Check if it's a summary request
            if data.get("summary") is True:
                await self.handle_summary_request(websocket, data)
            else:
                # Regular query request
                await self.handle_query_request(websocket, data)
                
        except json.JSONDecodeError as e:
            logger.error(f"Invalid JSON from {client_id}: {e}")
        except Exception as e:
            logger.error(f"Error processing message from {client_id}: {e}", exc_info=True)
    
    async def handle_query_request(self, websocket: WebSocketServerProtocol, data: Dict[str, Any]):
        """Handle a regular query request (ModelSendProtocol)"""
        token = data.get("token", "unknown")
        prompt = data.get("prompt", "")
        user = data.get("user", "unknown")
        history = data.get("history", [])
        
        logger.info(f"Processing query for user '{user}' (token: {token})")
        logger.debug(f"Prompt: {prompt}")
        logger.debug(f"History length: {len(history)}")
        
        # Send GEN_STARTED
        await self.send_message(websocket, {
            "type": "GEN_STARTED",
            "token": token,
            "generated_token": "",
            "content": "",
            "source": []
        })
        logger.debug(f"Sent GEN_STARTED to {token}")
        
        # Simulate token-by-token generation
        response_text = random.choice(self.MOCK_RESPONSES)
        
        # Add some context based on the prompt
        if len(prompt) > 0:
            response_text = f"关于「{prompt[:30]}{'...' if len(prompt) > 30 else ''}」的回答：{response_text}"
        
        # Send tokens one by one with delay
        tokens = self.split_into_tokens(response_text)
        for token_text in tokens:
            await asyncio.sleep(random.uniform(0.05, 0.15))  # Simulate generation delay
            await self.send_message(websocket, {
                "type": "NEW_TOKEN",
                "token": token,
                "generated_token": token_text,
                "content": "",
                "source": []
            })
        
        logger.debug(f"Sent {len(tokens)} tokens to {token}")
        
        # Send GEN_FINISHED with source documents
        sources = random.choice(self.MOCK_SOURCES)
        await self.send_message(websocket, {
            "type": "GEN_FINISHED",
            "token": token,
            "generated_token": "",
            "content": "",
            "source": sources
        })
        logger.info(f"Completed query for {token} with {len(sources)} sources")
    
    async def handle_summary_request(self, websocket: WebSocketServerProtocol, data: Dict[str, Any]):
        """Handle a summary request (ModelSummaryProtocol)"""
        token = data.get("token", "unknown")
        history = data.get("history", [])
        
        logger.info(f"Processing summary request for token '{token}'")
        logger.debug(f"History length: {len(history)}")
        
        # Simulate some processing delay
        await asyncio.sleep(random.uniform(0.5, 1.5))
        
        # Generate summary based on history
        if len(history) > 0:
            summary = f"对话包含 {len(history)} 条消息。{random.choice(self.MOCK_SUMMARIES)}"
        else:
            summary = "当前没有对话历史。"
        
        # Send SUMMARY response
        await self.send_message(websocket, {
            "type": "SUMMARY",
            "token": token,
            "generated_token": "",
            "content": summary,
            "source": []
        })
        logger.info(f"Sent summary to {token}: {summary}")
    
    def split_into_tokens(self, text: str) -> List[str]:
        """Split text into tokens for streaming simulation"""
        # Simple tokenization - split by characters but keep some together
        tokens = []
        i = 0
        while i < len(text):
            # Randomly decide token length (1-3 characters)
            length = random.randint(1, 3)
            token = text[i:i+length]
            if token:
                tokens.append(token)
            i += length
        return tokens
    
    async def send_message(self, websocket: WebSocketServerProtocol, data: Dict[str, Any]):
        """Send a message to the client"""
        message = json.dumps(data, ensure_ascii=False)
        await websocket.send(message)
    
    async def start_server(self):
        """Start the WebSocket server"""
        logger.info(f"Starting Mock Model Server on {HOST}:{PORT}")
        async with websockets.serve(self.handle_client, HOST, PORT):
            logger.info(f"✓ Mock Model Server is running on ws://{HOST}:{PORT}")
            logger.info("Waiting for connections from ChatPA server...")
            await asyncio.Future()  # Run forever


async def main():
    """Main entry point"""
    server = MockModelServer()
    try:
        await server.start_server()
    except KeyboardInterrupt:
        logger.info("Server shutting down...")
    except Exception as e:
        logger.error(f"Server error: {e}", exc_info=True)


if __name__ == "__main__":
    print("=" * 60)
    print("ChatPA Mock Model Server")
    print("=" * 60)
    print(f"Host: {HOST}")
    print(f"Port: {PORT}")
    print("=" * 60)
    print()
    
    try:
        asyncio.run(main())
    except KeyboardInterrupt:
        print("\n\nServer stopped by user")
