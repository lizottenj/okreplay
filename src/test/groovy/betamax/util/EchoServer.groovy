package betamax.util

import groovy.util.logging.Log4j
import java.util.concurrent.CountDownLatch

/**
 * A very simple socket server that listens for _one_ request and responds by just echoing back the request content. The
 * server shuts down after serving a single request but can be restarted by calling `start()` again.
 */
@Log4j
class EchoServer {

    private Thread t

    String start() {
        def host = InetAddress.localHost.hostAddress
        def port = 5000

		def readyLatch = new CountDownLatch(1)

        t = Thread.start {
            def server = new ServerSocket(port)
            try {
				readyLatch.countDown()
                server.accept { socket ->
                    log.debug "got request..."
                    socket.withStreams { input, output ->
                        output.withWriter { writer ->
                            writer << "HTTP/1.1 200 OK\n"
                            writer << "Content-Type: text/plain\n\n"
                            while (input.available()) {
                                writer << (char) input.read()
                            }
                        }
                    }
                }
            } catch(InterruptedException e) {
                log.info "interrupted"
            } finally {
                server.close()
            }
        }
		
		readyLatch.await()

        "http://$host:$port/"
    }

    void stop() {
        t.interrupt()
    }

}