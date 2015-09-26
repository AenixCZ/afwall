/**
 * Display/purge logs and toggle logging
 * 
 * Copyright (C) 2011-2013  Kevin Cernekee
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * @author Kevin Cernekee
 * @version 1.0
 */

package dev.ukanth.ufirewall.activity;

import android.content.Context;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.SubMenu;

import dev.ukanth.ufirewall.Api;
import dev.ukanth.ufirewall.service.NflogService;
import dev.ukanth.ufirewall.R;
import dev.ukanth.ufirewall.service.RootShell.RootCommand;
import dev.ukanth.ufirewall.log.LogInfo;
import dev.ukanth.ufirewall.util.G;

public class LogActivity extends DataDumpActivity {

	protected static final int MENU_CLEARLOG = 7;
	//protected static final int MENU_TOGGLE_LOG = 27;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTitle(getString(R.string.showlog_title));
		getSupportActionBar().setHomeButtonEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		sdDumpFile = "iptables.log";
	}

	protected void parseAndSet(Context ctx, String raw) {
		String cooked = LogInfo.parseLog(ctx, raw);
		if (cooked == null) {
			setData(getString(R.string.log_parse_error));
		} else {
			setData(cooked);
		}
	}
	
	protected void populateData(final Context ctx) {
		if (G.logTarget().equals("NFLOG")) {
			parseAndSet(ctx, NflogService.fetchLogs());
			return;
		}

		boolean enabled = Api.fetchLogs(ctx, new RootCommand().setLogging(true)
				.setReopenShell(true).setFailureToast(R.string.log_fetch_error)
				.setCallback(new RootCommand.Callback() {
					public void cbFunc(RootCommand state) {
						if (state.exitCode != 0) {
							setData(getString(R.string.log_fetch_error));
						} else {
							parseAndSet(ctx, state.res.toString());
						}
					}
				}));

		if (!enabled) {
			setData(getString(R.string.log_disabled));
		}
	}

	protected void populateMenu(SubMenu sub) {
		sub.add(0, MENU_CLEARLOG, 0, R.string.clear_log).setIcon(
				R.drawable.clearlog);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		final Context ctx = this;

		switch (item.getItemId()) {
		
		case android.R.id.home: {
			onBackPressed();
			return true;
		}
		case MENU_CLEARLOG:
			if (G.logTarget().equals("NFLOG")) {
				NflogService.clearLog();
				populateData(ctx);
				return true;
			}
			Api.clearLog(ctx,
					new RootCommand().setReopenShell(true)
							.setSuccessToast(R.string.log_cleared)
							.setFailureToast(R.string.log_clear_error)
							.setCallback(new RootCommand.Callback() {
								public void cbFunc(RootCommand state) {
									populateData(ctx);
								}
							}));
			return true;
		}
		return super.onOptionsItemSelected( item);
	}

	/*@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		// setupLogMenuItem(menu, G.enableFirewallLog());
		return super.onPrepareOptionsMenu(menu);
	}*/

	
}
