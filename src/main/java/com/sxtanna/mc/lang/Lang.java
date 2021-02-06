package com.sxtanna.mc.lang;

import co.aikar.locales.MessageKey;
import co.aikar.locales.MessageKeyProvider;
import org.jetbrains.annotations.NotNull;

import java.text.NumberFormat;
import java.util.Locale;

public enum Lang implements MessageKeyProvider
{

	GAINS__MESSAGE__HOLO,
	GAINS__MESSAGE__CHAT,
	GAINS__MESSAGE__LIMIT,

	PLUGIN__RELOAD,

	BALANCE__SENDER,
	BALANCE__TARGET,


	PAY__FAILURE,
	PAY__SUCCESS_SENDER,
	PAY__SUCCESS_TARGET,

	REM__SUCCESS,
	REM__FAILURE,

	ADD__SUCCESS,
	ADD__FAILURE,

	SET__FAILURE,
	SET__SUCCESS,


	ERROR__RELOAD,
	ERROR__GENERIC__NO_PERMISSION,
	ERROR__GENERIC__PLAYER_NEEDED,
	ERROR__NO_USER_IN_BANK;


	public static final NumberFormat CURRENCY_FORMAT = NumberFormat.getCurrencyInstance();


	public static String formatCurrency(@NotNull final Number value)
	{
		final String format = CURRENCY_FORMAT.format(value);
		return format.substring(1, format.length() - 3);
	}


	@NotNull
	private final MessageKey key = MessageKey.of(name().toLowerCase().replace("__", ".").replace("_", "-"));


	@NotNull
	@Override
	public final MessageKey getMessageKey()
	{
		return key;
	}

}
