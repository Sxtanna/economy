package com.sxtanna.mc.cmds;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Flags;
import co.aikar.commands.annotation.HelpCommand;
import co.aikar.commands.annotation.Optional;
import co.aikar.commands.annotation.Subcommand;
import com.sxtanna.mc.EconomyPlugin;
import com.sxtanna.mc.cont.EconomyController;
import com.sxtanna.mc.data.User;
import com.sxtanna.mc.lang.Lang;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigInteger;
import java.util.function.Consumer;

import static com.sxtanna.mc.util.Optionals.handle;

@SuppressWarnings("CodeBlock2Expr")
@CommandAlias("token|tokens")
public final class EconomyCommand extends BaseCommand
{

	@NotNull
	private final EconomyPlugin plugin;


	public EconomyCommand(@NotNull final EconomyPlugin plugin)
	{
		this.plugin = plugin;
	}


	@Default
	@HelpCommand
	@CommandPermission("tokens.help")
	public void help(@NotNull final CommandHelp help)
	{
		help.showHelp();
	}


	@Subcommand("reload")
	@CommandPermission("tokens.reload")
	public void reload(@NotNull final CommandSender sender)
	{
		handle(plugin.reload(),
			   () ->
			   {
				   plugin.sendMessage(sender, Lang.PLUGIN__RELOAD);
			   },
			   (error) ->
			   {
				   plugin.sendMessage(sender, Lang.ERROR__RELOAD,

									  "{reason}",
									  error.getMessage());
			   });
	}


	@Subcommand("add|give")
	@CommandPermission("tokens.add")
	public void add(@NotNull final CommandSender sender, @NotNull final BigInteger amount, @Flags("other") @NotNull final Player target)
	{
		controller(sender, controller -> {
			handle(controller.findUserInDefaultBank(target.getUniqueId()),
				   () -> {

					   plugin.sendMessage(sender, Lang.ERROR__NO_USER_IN_BANK,

										  "{target}",
										  target.getName());

				   },
				   (targetUser) -> {
					   targetUser.add(amount).handle(
							   targetAddFailure -> {

								   plugin.sendMessage(sender, Lang.ADD__FAILURE,

													  "{target}",
													  target.getName(),

													  "{amount}",
													  Lang.formatCurrency(targetAddFailure.getAmount()),

													  "{reason}",
													  targetAddFailure.getMessage());

							   },
							   targetAddSuccess -> {

								   plugin.sendMessage(sender, Lang.ADD__SUCCESS,

													  "{target}",
													  target.getName(),

													  "{amount}",
													  Lang.formatCurrency(targetAddSuccess.getAmount()),

													  "{balance}",
													  Lang.formatCurrency(targetUser.getBalance()));

							   }
					   );
				   });
		});
	}

	@Subcommand("remove|take")
	@CommandPermission("tokens.remove")
	public void rem(@NotNull final CommandSender sender, @NotNull final BigInteger amount, @Flags("other") @NotNull final Player target)
	{
		controller(sender, controller -> {
			handle(controller.findUserInDefaultBank(target.getUniqueId()),
				   () -> {

					   plugin.sendMessage(sender, Lang.ERROR__NO_USER_IN_BANK,

										  "{target}",
										  target.getName());

				   },
				   (targetUser) -> {


					   targetUser.sub(amount).handle(
							   targetSubFailure -> {

								   plugin.sendMessage(sender, Lang.REM__FAILURE,

													  "{target}",
													  target.getName(),

													  "{amount}",
													  Lang.formatCurrency(targetSubFailure.getAmount()),

													  "{reason}",
													  targetSubFailure.getMessage());

							   },
							   targetSubSuccess -> {

								   plugin.sendMessage(sender, Lang.REM__SUCCESS,

													  "{target}",
													  target.getName(),

													  "{amount}",
													  Lang.formatCurrency(targetSubSuccess.getAmount()),

													  "{balance}",
													  Lang.formatCurrency(targetUser.getBalance()));

							   }
					   );

				   });
		});
	}

	@Subcommand("set")
	@CommandPermission("tokens.set")
	public void set(@NotNull final CommandSender sender, @NotNull final BigInteger amount, @Flags("other") @NotNull final Player target)
	{
		controller(sender, controller -> {
			handle(controller.findUserInDefaultBank(target.getUniqueId()),
				   () -> {

					   plugin.sendMessage(sender, Lang.ERROR__NO_USER_IN_BANK,

										  "{target}",
										  target.getName());

				   },
				   (targetUser) -> {

					   if (amount.compareTo(BigInteger.ZERO) < 0)
					   {

						   plugin.sendMessage(sender, Lang.SET__FAILURE,

											  "{target}",
											  target.getName(),

											  "{amount}",
											  Lang.formatCurrency(amount),

											  "{reason}",
											  "Balance cannot be negative!");

						   return;

					   }


					   targetUser.setBalance(amount);


					   plugin.sendMessage(sender, Lang.SET__SUCCESS,

										  "{target}",
										  target.getName(),

										  "{amount}",
										  Lang.formatCurrency(amount),

										  "{balance}",
										  Lang.formatCurrency(targetUser.getBalance()));

				   });
		});
	}

	@Subcommand("pay")
	@CommandPermission("tokens.pay")
	public void pay(@NotNull final Player sender, @NotNull final BigInteger amount, @Flags("other") @NotNull final Player target)
	{
		controller(sender, controller -> {

			handle(controller.findUserInDefaultBank(sender.getUniqueId()),
				   () -> {

					   plugin.sendMessage(sender, Lang.ERROR__NO_USER_IN_BANK,

										  "{target}",
										  sender.getName());

				   },
				   (senderUser) -> {

					   senderUser.sub(amount).handle(
							   senderSubFailure -> {

								   plugin.sendMessage(sender, Lang.PAY__FAILURE,

													  "{target}",
													  target.getName(),

													  "{amount}",
													  Lang.formatCurrency(senderSubFailure.getAmount()),

													  "{reason}",
													  senderSubFailure.getMessage());


							   },
							   senderSubSuccess -> {


								   handle(controller.findUserInDefaultBank(target.getUniqueId()),
										  () -> {

											  plugin.sendMessage(sender, Lang.ERROR__NO_USER_IN_BANK,

																 "{target}",
																 target.getName());

										  },
										  (targetUser) -> {
											  targetUser.add(amount).handle(
													  targetAddFailure -> {
														  plugin.sendMessage(sender, Lang.PAY__FAILURE,

																			 "{target}",
																			 target.getName(),

																			 "{amount}",
																			 Lang.formatCurrency(targetAddFailure.getAmount()),

																			 "{reason}",
																			 targetAddFailure.getMessage());


														  senderUser.add(amount);

													  },
													  targetAddSuccess -> {


														  plugin.sendMessage(sender, Lang.PAY__SUCCESS_SENDER,

																			 "{target}",
																			 target.getName(),

																			 "{amount}",
																			 Lang.formatCurrency(targetAddSuccess.getAmount()),

																			 "{sender_balance}",
																			 Lang.formatCurrency(senderUser.getBalance()));

														  plugin.sendMessage(target, Lang.PAY__SUCCESS_TARGET,

																			 "{sender}",
																			 sender.getName(),

																			 "{amount}",
																			 Lang.formatCurrency(targetAddSuccess.getAmount()),

																			 "{target_balance}",
																			 Lang.formatCurrency(targetUser.getBalance()));

													  }
											  );
										  });
							   });
				   });
		});
	}


	@Subcommand("balance|bal")
	@CommandCompletion("@players")
	@CommandPermission("tokens.balance")
	public void balance(@NotNull final CommandSender sender, @Optional @Flags("other") @Nullable final Player target)
	{

		final Player user;

		if (target != null)
		{
			if (!sender.hasPermission("tokens.balance.other"))
			{

				plugin.sendMessage(sender, Lang.ERROR__GENERIC__NO_PERMISSION);

				return;
			}

			user = target;
		}
		else
		{
			if (!(sender instanceof Player))
			{

				plugin.sendMessage(sender, Lang.ERROR__GENERIC__PLAYER_NEEDED);

				return;
			}

			user = ((Player) sender);
		}

		controller(sender, controller -> {
			handle(controller.findUserInDefaultBank(user.getUniqueId()).map(User::getBalance),

				   () -> {


					   plugin.sendMessage(sender, Lang.ERROR__NO_USER_IN_BANK,

										  "{target}",
										  user.getName());

				   },

				   (balance) -> {

					   final Lang key = sender.equals(user) ? Lang.BALANCE__SENDER : Lang.BALANCE__TARGET;

					   plugin.sendMessage(sender, key,

										  "{target}",
										  user.getName(),

										  "{balance}",
										  Lang.formatCurrency(balance));

				   });
		});
	}


	private void controller(@NotNull final CommandSender sender, @NotNull Consumer<EconomyController> consumer)
	{
		handle(plugin.getController(),

			   () -> { /* todo: send controller unavailable? */ }, // this should never happen

			   consumer);
	}

}
