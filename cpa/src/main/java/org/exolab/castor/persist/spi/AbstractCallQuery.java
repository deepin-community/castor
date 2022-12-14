package org.exolab.castor.persist.spi;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.castor.core.util.Messages;
import org.castor.jdo.engine.SQLTypeInfos;
import org.castor.persist.ProposedEntity;
import org.exolab.castor.jdo.PersistenceException;
import org.exolab.castor.mapping.AccessMode;

public abstract class AbstractCallQuery implements PersistenceQuery {
    /** The <a href="http://jakarta.apache.org/commons/logging/">Jakarta Commons
     *  Logging</a> instance used for all logging. */
    private static Log _log = LogFactory.getFactory().getInstance(AbstractCallQuery.class);

    private int[] _sqlTypes;

    protected PreparedStatement _stmt;

    protected ResultSet _rs;

    protected Identity _lastIdentity;

    private final Class<?>[] _types;

    protected final Object[] _values;

    private final Class<?> _javaClass;

    protected final String _call;

    protected AbstractCallQuery(final String call, final Class<?>[] types,
            final Class<?> javaClass, final int[] sqlTypes) {
        _types = types;
        _javaClass = javaClass;
        _sqlTypes = sqlTypes;
        _values = new Object[_types.length];
        _call = "{call " + call + "}";
    }

    protected abstract boolean nextRow() throws SQLException;

    /**
     * @inheritDoc
     */
    public void fetch(final ProposedEntity proposedObject)
    throws PersistenceException {
        try {
            // Load all the fields of the object including one-one relations
            // index 0 belongs to the identity
            for (int i = 1; i < _sqlTypes.length; ++i) {
                proposedObject.setField(
                        SQLTypeInfos.getValue(_rs, i + 1, _sqlTypes[i]), i - 1);
            }
            if (nextRow()) {
                _lastIdentity = new Identity(SQLTypeInfos.getValue(_rs, 1, _sqlTypes[0]));
            } else {
                _lastIdentity = null;
            }
        } catch (SQLException except) {
            throw new PersistenceException(Messages.format("persist.nested", except));
        }
    }

    public void close() {
        if (_rs != null) {
            try {
                _rs.close();
            } catch (SQLException except) {
                _log.warn(Messages.message("persist.rsClosingFailed"), except);
            }
            _rs = null;
        }
        if (_stmt != null) {
            try {
                _stmt.close();
            } catch (SQLException except) {
                _log.warn(Messages.message("persist.stClosingFailed"), except);
            }
            _stmt = null;
        }
    }

    public int getParameterCount() {
        return _types.length;
    }

    public Class<?> getParameterType(final int index)
            throws ArrayIndexOutOfBoundsException {
        return _types[index];
    }

    public void setParameter(final int index, final Object value)
            throws ArrayIndexOutOfBoundsException, IllegalArgumentException {
        _values[index] = value;
    }

    public boolean absolute(final int row) throws PersistenceException {
        return false;
    }

    public int size() throws PersistenceException {
        return 0;
    }

    public Class<?> getResultType() {
        return _javaClass;
    }

    public void execute(final Object conn, final AccessMode accessMode, final boolean scrollable)
    throws PersistenceException {
        execute(conn, accessMode);
    }

    protected abstract void execute(final Object conn, final AccessMode accessMode)
    throws PersistenceException;

    public Identity nextIdentity(final Identity identity) throws PersistenceException {
        try {
            if (_lastIdentity == null) {
                if (!nextRow()) { return null; }
                _lastIdentity = new Identity(SQLTypeInfos.getValue(_rs, 1, _sqlTypes[0]));
                return _lastIdentity;
            }

            while (_lastIdentity.equals(identity)) {
                if (!nextRow()) {
                    _lastIdentity = null;
                    return null;
                }
                _lastIdentity = new Identity(SQLTypeInfos.getValue(_rs, 1, _sqlTypes[0]));
            }
            return _lastIdentity;
        } catch (SQLException except) {
            _lastIdentity = null;
            throw new PersistenceException(Messages.format("persist.nested", except));
        }
    }
}
