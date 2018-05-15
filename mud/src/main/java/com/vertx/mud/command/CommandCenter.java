package com.vertx.mud.command;

import io.vertx.core.AbstractVerticle;
import io.vertx.example.util.Runner;
import io.vertx.ext.shell.ShellService;
import io.vertx.ext.shell.ShellServiceOptions;
import io.vertx.ext.shell.command.Command;
import io.vertx.ext.shell.command.CommandBuilder;
import io.vertx.ext.shell.command.CommandRegistry;
import io.vertx.ext.shell.term.TelnetTermOptions;

/*
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class CommandCenter extends AbstractVerticle {

	@Override
	public void start() throws Exception {

		Command helloWorld = CommandBuilder.command("hello-world").processHandler(process -> {
			process.write("hello world\n");
			process.end();
		}).build(vertx);

		ShellService service = ShellService.create(vertx,
				new ShellServiceOptions().setTelnetOptions(new TelnetTermOptions().setHost("localhost").setPort(3000)));
		CommandRegistry.getShared(vertx).registerCommand(helloWorld);
		service.start(ar -> {
			if (!ar.succeeded()) {
				ar.cause().printStackTrace();
			}
		});
	}
}
